package com.yx.config;

import com.yx.cache.CacheManager;
import com.yx.entity.Admin;
import com.yx.entity.BookInfo;
import com.yx.entity.LendList;
import com.yx.entity.Message;
import com.yx.entity.ReaderInfo;
import com.yx.repository.AdminRepository;
import com.yx.repository.BookInfoRepository;
import com.yx.repository.LendListRepository;
import com.yx.repository.MessageRepository;
import com.yx.repository.NoticeRepository;
import com.yx.repository.ReaderInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataConsistencyRunner implements ApplicationRunner {
    private static final String LEND_STATISTICS_CACHE = "lend:statistics";
    private final LendListRepository lendListRepository;
    private final ReaderInfoRepository readerInfoRepository;
    private final BookInfoRepository bookInfoRepository;
    private final MessageRepository messageRepository;
    private final NoticeRepository noticeRepository;
    private final AdminRepository adminRepository;
    private final CacheManager cacheManager;

    public DataConsistencyRunner(LendListRepository lendListRepository,
                                 ReaderInfoRepository readerInfoRepository,
                                 BookInfoRepository bookInfoRepository,
                                 MessageRepository messageRepository,
                                 NoticeRepository noticeRepository,
                                 AdminRepository adminRepository,
                                 CacheManager cacheManager) {
        this.lendListRepository = lendListRepository;
        this.readerInfoRepository = readerInfoRepository;
        this.bookInfoRepository = bookInfoRepository;
        this.messageRepository = messageRepository;
        this.noticeRepository = noticeRepository;
        this.adminRepository = adminRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        cleanupOrphans();
        syncCounters();
        syncSex();
        clearCaches();
    }

    private void cleanupOrphans() {
        int deletedLends = 0;
        List<LendList> lends = lendListRepository.findAll();
        for (LendList l : lends) {
            if (l.getReaderId() == null || l.getBookId() == null) {
                lendListRepository.delete(l);
                deletedLends++;
                continue;
            }
            if (!readerInfoRepository.existsById(l.getReaderId()) || !bookInfoRepository.existsById(l.getBookId())) {
                lendListRepository.delete(l);
                deletedLends++;
            }
        }

        int deletedMessages = 0;
        List<Message> messages = messageRepository.findAll();
        for (Message m : messages) {
            // 读者不存在
            if (m.getReaderId() == null || !readerInfoRepository.existsById(m.getReaderId())) {
                messageRepository.delete(m);
                deletedMessages++;
                continue;
            }
            // 关联的公告已不存在
            if (m.getNoticeId() != null && !noticeRepository.existsById(m.getNoticeId())) {
                messageRepository.delete(m);
                deletedMessages++;
            }
        }

        if (deletedLends > 0 || deletedMessages > 0) {
            log.warn("数据一致性清理完成: 删除借阅记录 {} 条, 删除消息 {} 条", deletedLends, deletedMessages);
        } else {
            log.info("数据一致性清理完成: 未发现孤立数据");
        }
    }

    private void syncCounters() {
        Map<Long, Integer> bookBorrowed = new HashMap<>();
        Map<Long, Integer> readerBorrowed = new HashMap<>();
        Map<Long, Double> readerFine = new HashMap<>();

        List<LendList> lends = lendListRepository.findAll();
        for (LendList l : lends) {
            Long readerId = l.getReaderId();
            Long bookId = l.getBookId();
            Integer bt = l.getBackType();
            Double fine = l.getFineAmount();

            if (readerId != null) {
                if (bt != null && (bt == LendList.STATUS_BORROWED || bt == LendList.STATUS_OVERDUE)) {
                    readerBorrowed.put(readerId, readerBorrowed.getOrDefault(readerId, 0) + 1);
                }
                if (bt != null && (bt == LendList.STATUS_OVERDUE || bt == LendList.STATUS_RETURNED || bt == LendList.STATUS_DAMAGED)) {
                    if (fine != null && fine > 0) {
                        readerFine.put(readerId, readerFine.getOrDefault(readerId, 0.0) + fine);
                    }
                }
            }

            if (bookId != null) {
                if (bt != null && (bt == LendList.STATUS_BORROWED || bt == LendList.STATUS_OVERDUE)) {
                    bookBorrowed.put(bookId, bookBorrowed.getOrDefault(bookId, 0) + 1);
                }
            }
        }

        List<ReaderInfo> readers = readerInfoRepository.findAll();
        int updatedReaders = 0;
        for (ReaderInfo r : readers) {
            int bc = readerBorrowed.getOrDefault(r.getId(), 0);
            double fa = readerFine.getOrDefault(r.getId(), 0.0);
            int curBc = r.getBorrowCount() != null ? r.getBorrowCount() : 0;
            double curFa = r.getFineAmount() != null ? r.getFineAmount() : 0.0;

            boolean changed = false;
            if (curBc != bc) {
                r.setBorrowCount(bc);
                changed = true;
            }
            if (Double.compare(curFa, fa) != 0) {
                r.setFineAmount(fa);
                changed = true;
            }
            if (r.getSex() == null || (r.getSex() != 1 && r.getSex() != 2)) {
                r.setSex(1);
                changed = true;
            }

            if (changed) {
                readerInfoRepository.save(r);
                updatedReaders++;
            }
        }

        List<BookInfo> books = bookInfoRepository.findAll();
        int updatedBooks = 0;
        for (BookInfo b : books) {
            int bc = bookBorrowed.getOrDefault(b.getId(), 0);
            int curBc = b.getBorrowCount() != null ? b.getBorrowCount() : 0;
            if (curBc != bc) {
                b.setBorrowCount(bc);
                bookInfoRepository.save(b);
                updatedBooks++;
            }
        }

        if (updatedReaders > 0 || updatedBooks > 0) {
            log.warn("数据一致性同步完成: 更新读者 {} 条, 更新图书 {} 条", updatedReaders, updatedBooks);
        } else {
            log.info("数据一致性同步完成: 无需更新计数");
        }
    }

    private void syncSex() {
        int updatedAdmins = 0;
        List<Admin> admins = adminRepository.findAll();
        for (Admin a : admins) {
            Integer sex = a.getSex();
            if (sex == null || (sex != 1 && sex != 2)) {
                a.setSex(1);
                adminRepository.save(a);
                updatedAdmins++;
            }
        }
        if (updatedAdmins > 0) {
            log.warn("数据一致性同步完成: 更新管理员性别 {} 条", updatedAdmins);
        }
    }

    private void clearCaches() {
        cacheManager.delete(LEND_STATISTICS_CACHE);
        cacheManager.delete("book:statistics");
        cacheManager.deletePattern("book:list:*");
        cacheManager.deletePattern("reader:*");
        cacheManager.deletePattern("book:*");
    }
}
