package com.yx.service.impl;

import com.yx.cache.CacheManager;
import com.yx.entity.Admin;
import com.yx.entity.BookInfo;
import com.yx.entity.LendList;
import com.yx.entity.ReaderInfo;
import com.yx.exception.BusinessException;
import com.yx.repository.AdminRepository;
import com.yx.repository.BookInfoRepository;
import com.yx.repository.LendListRepository;
import com.yx.repository.ReaderInfoRepository;
import com.yx.service.BookInfoService;
import com.yx.service.LendListService;
import com.yx.service.MessageService;
import com.yx.service.ReaderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 借阅管理服务实现
 */
@Slf4j
@Service
@Transactional
public class LendListServiceImpl implements LendListService {

    @Autowired
    private LendListRepository lendListRepository;

    @Autowired
    private BookInfoService bookInfoService;

    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Autowired
    private ReaderInfoService readerInfoService;

    @Autowired
    private ReaderInfoRepository readerInfoRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CacheManager cacheManager;

    private static final String LEND_STATISTICS_CACHE = "lend:statistics";
    private static final long CACHE_TIMEOUT = 3600;
    private static final int DEFAULT_BORROW_DAYS = 7;
    private static final double FINE_PER_DAY = 1.0; // 每天罚款1元

    /**
     * 分页查询所有借阅记录
     */
    @Override
    public Page<LendList> queryAll(Integer page, Integer limit) {
        return queryAll(page, limit, null, null, null, null, null);
    }

    /**
     * 条件分页查询
     */
    @Override
    public Page<LendList> queryAll(Integer page, Integer limit, String readerName, String bookName, Integer status,
                                   java.util.Date startDate, java.util.Date endDate) {
        refreshOverdueStates();
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;

        Specification<LendList> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                if (status == LendList.STATUS_BORROWED) {
                    predicates.add(root.get("backType").in(LendList.STATUS_BORROWED, LendList.STATUS_OVERDUE));
                } else if (status == LendList.STATUS_OVERDUE) {
                    predicates.add(cb.equal(root.get("backType"), LendList.STATUS_OVERDUE));
                } else {
                    predicates.add(cb.equal(root.get("backType"), status));
                }
            }

            String rn = readerName == null ? null : readerName.trim();
            String bn = bookName == null ? null : bookName.trim();
            boolean hasReaderKeyword = rn != null && !rn.isEmpty();
            boolean hasBookKeyword = bn != null && !bn.isEmpty();

            if (hasReaderKeyword && hasBookKeyword && rn.equals(bn)) {
                String keyword = rn;
                Join<LendList, ReaderInfo> readerJoin = root.join("reader", JoinType.LEFT);
                Join<LendList, BookInfo> bookJoin = root.join("book", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(readerJoin.get("realName"), "%" + keyword + "%"),
                        cb.like(readerJoin.get("readerNumber"), "%" + keyword + "%"),
                        cb.like(bookJoin.get("name"), "%" + keyword + "%"),
                        cb.like(bookJoin.get("author"), "%" + keyword + "%"),
                        cb.like(bookJoin.get("location"), "%" + keyword + "%")
                ));
            } else {
                if (hasReaderKeyword) {
                    Join<LendList, ReaderInfo> readerJoin = root.join("reader", JoinType.LEFT);
                    predicates.add(cb.or(
                            cb.like(readerJoin.get("realName"), "%" + rn + "%"),
                            cb.like(readerJoin.get("readerNumber"), "%" + rn + "%")
                    ));
                }

                if (hasBookKeyword) {
                    Join<LendList, BookInfo> bookJoin = root.join("book", JoinType.LEFT);
                    predicates.add(cb.or(
                            cb.like(bookJoin.get("name"), "%" + bn + "%"),
                            cb.like(bookJoin.get("author"), "%" + bn + "%"),
                            cb.like(bookJoin.get("location"), "%" + bn + "%")
                    ));
                }
            }
            
            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createTime"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(
                Sort.Order.desc("lendDate"),
                Sort.Order.desc("createTime"),
                Sort.Order.desc("id")
        );
        Page<LendList> result = lendListRepository.findAll(spec, PageRequest.of(page - 1, limit, sort));
        result.getContent().forEach(this::populateDetails);
        return result;
    }

    /**
     * 查询读者的借阅记录
     */
    @Override
    public Page<LendList> queryByReader(Long readerId, Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        Sort sort = Sort.by(
                Sort.Order.desc("lendDate"),
                Sort.Order.desc("createTime"),
                Sort.Order.desc("id")
        );
        Page<LendList> result = lendListRepository.findByReaderId(readerId, PageRequest.of(page - 1, limit, sort));
        result.getContent().forEach(this::populateDetails);
        return result;
    }

    /**
     * 查询图书的借阅记录
     */
    @Override
    public List<LendList> queryByBook(Long bookId) {
        List<LendList> list = lendListRepository.findByBookId(bookId);
        list.forEach(this::populateDetails);
        return list;
    }

    /**
     * 查询未还的借阅记录
     */
    @Override
    public List<LendList> queryUnreturned(Long readerId) {
        List<LendList> list = lendListRepository.findByReaderIdAndBackType(readerId, LendList.STATUS_BORROWED);
        list.addAll(lendListRepository.findByReaderIdAndBackType(readerId, LendList.STATUS_OVERDUE));
        list.forEach(this::populateDetails);
        return list;
    }

    /**
     * 查询逾期借阅
     */
    @Override
    public Page<LendList> queryOverdue(Integer page, Integer limit) {
        refreshOverdueStates();
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        Sort sort = Sort.by(
                Sort.Order.desc("lendDate"),
                Sort.Order.desc("createTime"),
                Sort.Order.desc("id")
        );
        Page<LendList> result = lendListRepository.findByBackType(LendList.STATUS_OVERDUE, PageRequest.of(page - 1, limit, sort));
        result.getContent().forEach(this::populateDetails);
        return result;
    }

    private void populateDetails(LendList lend) {
        if (lend == null) return;
        
        if (lend.getReader() != null) {
            lend.setReaderName(lend.getReader().getRealName());
            lend.setReaderNumber(lend.getReader().getReaderNumber());
            lend.setReaderDepartment(lend.getReader().getDepartment());
            lend.setReaderTel(lend.getReader().getTel());
        } else {
            try {
                ReaderInfo reader = readerInfoService.getById(lend.getReaderId());
                lend.setReaderName(reader != null ? reader.getRealName() : "未知读者");
                lend.setReaderNumber(reader != null ? reader.getReaderNumber() : null);
                lend.setReaderDepartment(reader != null ? reader.getDepartment() : null);
                lend.setReaderTel(reader != null ? reader.getTel() : null);
            } catch (Exception e) {
                lend.setReaderName("未知读者");
                lend.setReaderNumber(null);
                lend.setReaderDepartment(null);
                lend.setReaderTel(null);
            }
        }

        if (lend.getBook() != null) {
            lend.setBookTitle(lend.getBook().getName());
        } else {
            try {
                BookInfo book = bookInfoService.getById(lend.getBookId());
                lend.setBookTitle(book != null ? book.getName() : "未知图书");
            } catch (Exception e) {
                lend.setBookTitle("未知图书");
            }
        }
        
        if (lend.getAuditAdmin() != null) {
            lend.setAuditAdminName(lend.getAuditAdmin().getUsername());
        } else if (lend.getAuditAdminId() != null) {
            try {
                Admin admin = adminRepository.findById(lend.getAuditAdminId()).orElse(null);
                lend.setAuditAdminName(admin != null ? admin.getUsername() : "ID:" + lend.getAuditAdminId());
            } catch (Exception e) {
                lend.setAuditAdminName("ID:" + lend.getAuditAdminId());
            }
        }
        
        Integer backType = lend.getBackType();
        if (backType != null && backType == LendList.STATUS_BORROWED) {
            if (lend.getShouldBackDate() != null && new Date().after(lend.getShouldBackDate())) {
                lend.setStatus(LendList.STATUS_OVERDUE);
            } else {
                lend.setStatus(backType);
            }
        } else {
            lend.setStatus(backType);
        }
    }

    /**
     * 获取单个借阅记录
     */
    @Override
    public LendList getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("借阅ID不能为空");
        }
        Optional<LendList> lendOpt = lendListRepository.findById(id);
        if (!lendOpt.isPresent()) {
            throw new BusinessException("借阅记录不存在");
        }
        LendList lend = lendOpt.get();
        populateDetails(lend);
        return lend;
    }

    private static final int MAX_BORROW_LIMIT = 5;

    /**
     * 借书申请
     */
    @Override
    @Transactional
    public void borrowBook(Long readerId, Long bookId, Integer count, Integer borrowDays, Date lendDate, Date shouldBackDate) {
        if (readerId == null || bookId == null) {
            throw new BusinessException("读者或图书ID不能为空");
        }

        // 1. 校验读者状态
        ReaderInfo reader = readerInfoService.getById(readerId);
        if (reader.getStatus() != null && reader.getStatus() == 1) {
            throw new BusinessException("该读者已被禁用，不能借书");
        }

        // 1.1 校验逾期限制
        List<LendList> unreturned = new ArrayList<>();
        unreturned.addAll(lendListRepository.findByReaderIdAndBackType(readerId, LendList.STATUS_BORROWED));
        unreturned.addAll(lendListRepository.findByReaderIdAndBackType(readerId, LendList.STATUS_OVERDUE));
        boolean hasOverdue = unreturned.stream().anyMatch(l -> 
            l.getShouldBackDate() != null && new Date().after(l.getShouldBackDate())
        );
        if (hasOverdue) {
            throw new BusinessException("您有逾期未还的书籍，请先归还并处理罚款后再借书");
        }

        // 1.2 校验罚款限制
        if (reader.getFineAmount() != null && reader.getFineAmount() > 0) {
            throw new BusinessException("您有未缴纳的罚款（金额：" + reader.getFineAmount() + "元），请先缴纳罚款后再借书");
        }

        // 2. 校验最大借阅数量限制
        Integer currentBorrowCount = reader.getBorrowCount() != null ? reader.getBorrowCount() : 0;
        if (currentBorrowCount + count > MAX_BORROW_LIMIT) {
            throw new BusinessException("超过最大借阅限制（最多5本）");
        }

        // 3. 校验是否已借过同本书且未归还（包括借出中、逾期、待审核状态）
        List<LendList> allUnreturned = new ArrayList<>();
        allUnreturned.addAll(unreturned);
        allUnreturned.addAll(lendListRepository.findByReaderIdAndBackType(readerId, LendList.STATUS_PENDING));
        boolean alreadyBorrowed = allUnreturned.stream().anyMatch(l -> l.getBookId().equals(bookId));
        if (alreadyBorrowed) {
            throw new BusinessException("您已借阅或申请过此书且尚未归还/处理，不能重复借阅");
        }

        // 4. 使用行锁锁定图书记录并校验库存
        BookInfo book = bookInfoRepository.findByIdWithLock(bookId)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        
        if (book.getStatus() != null && book.getStatus() == 2) {
            throw new BusinessException("该图书已禁用");
        }

        int stock = book.getStock() != null ? book.getStock() : 0;
        int borrowed = book.getBorrowCount() != null ? book.getBorrowCount() : 0;
        int availableCount = stock - borrowed;

        // 判断是否可以直接借阅
        boolean canBorrowDirectly = availableCount >= count && (book.getIsReviewRequired() == null || book.getIsReviewRequired() == 0);
        
        // 5. 设置借阅天数逻辑
        if (lendDate == null) lendDate = new Date();
        if (shouldBackDate == null) {
            if (borrowDays == null || borrowDays <= 0) {
                borrowDays = DEFAULT_BORROW_DAYS;
            }
            shouldBackDate = Date.from(LocalDateTime.now().plusDays(borrowDays).atZone(ZoneId.systemDefault()).toInstant());
        }

        boolean needsReview = !canBorrowDirectly;
        
        if (!needsReview) {
            book.setBorrowCount(borrowed + count);
            bookInfoRepository.saveAndFlush(book);
            
            reader.setBorrowCount(currentBorrowCount + count);
            readerInfoService.update(reader);
        }

        // 7. 创建借阅记录
        LendList lend = LendList.builder()
                .bookId(bookId)
                .readerId(readerId)
                .lendDate(lendDate) // 无论是否需要审核，都保存预计取书时间
                .shouldBackDate(shouldBackDate) // 无论是否需要审核，都保存预计还书时间
                .backType(needsReview ? LendList.STATUS_PENDING : LendList.STATUS_BORROWED)
                .fineAmount(0.0)
                .build();

        lendListRepository.saveAndFlush(lend);

        // 清除相关缓存，确保前端能看到最新的库存和借阅状态
        // 1. 清除借阅统计缓存
        cacheManager.delete(LEND_STATISTICS_CACHE);
        // 2. 清除特定书籍详情缓存
        cacheManager.delete("book:" + bookId);
        // 3. 清除特定读者信息缓存
        cacheManager.delete("reader:" + readerId);
        // 4. 清除图书列表分页缓存（通过模糊匹配删除所有分页缓存）
        cacheManager.deletePattern("book:list:*");
        // 5. 清除图书统计信息缓存
        cacheManager.delete("book:statistics");

        log.info("借书流程完成: readerId={}, bookId={}, status={}", readerId, bookId, lend.getBackType());
    }

    /**
     * 审核借书申请
     */
    @Override
    @Transactional
    public void auditBorrow(Long lendId, Integer status, Long adminId, String remarks) {
        LendList lend = lendListRepository.findByIdWithLock(lendId)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));

        if (lend.getBackType() != LendList.STATUS_PENDING) {
            throw new BusinessException("该记录不是待审核状态");
        }

        lend.setAuditAdminId(adminId);
        lend.setAuditDate(new Date());

        if (status == LendList.STATUS_BORROWED) { // 同意
            // 校验库存是否足够
            BookInfo book = bookInfoRepository.findByIdWithLock(lend.getBookId())
                    .orElseThrow(() -> new BusinessException("图书不存在"));
            
            int stock = book.getStock() != null ? book.getStock() : 0;
            int borrowed = book.getBorrowCount() != null ? book.getBorrowCount() : 0;
            if (stock <= borrowed) {
                throw new BusinessException("图书库存不足，无法通过审核");
            }

            lend.setBackType(LendList.STATUS_BORROWED);
            lend.setLendDate(new Date());
            // 默认30天借期
            lend.setShouldBackDate(Date.from(LocalDateTime.now().plusDays(DEFAULT_BORROW_DAYS).atZone(ZoneId.systemDefault()).toInstant()));
            
            // 更新读者借书数
            ReaderInfo reader = readerInfoService.getById(lend.getReaderId());
            reader.setBorrowCount((reader.getBorrowCount() != null ? reader.getBorrowCount() : 0) + 1);
            readerInfoService.update(reader);
            
            // 更新图书已借出数量
            book.setBorrowCount(borrowed + 1);
            bookInfoRepository.save(book);
            
        } else if (status == LendList.STATUS_REJECTED) { // 拒绝
            lend.setBackType(LendList.STATUS_REJECTED);
            if (remarks != null && !remarks.trim().isEmpty()) {
                String old = lend.getExceptRemarks();
                lend.setExceptRemarks((old == null || old.isEmpty()) ? remarks.trim() : (old + "；" + remarks.trim()));
            }
        } else {
            throw new BusinessException("非法的审核状态");
        }

        lendListRepository.saveAndFlush(lend);
        
        // 清除缓存
        cacheManager.delete(LEND_STATISTICS_CACHE);
        cacheManager.delete("book:" + lend.getBookId());
        cacheManager.delete("reader:" + lend.getReaderId());
        cacheManager.deletePattern("book:list:*");
        cacheManager.delete("book:statistics");
    }

    /**
     * 还书
     */
    @Override
    @Transactional
    public void returnBook(Long lendId) {
        LendList lend = lendListRepository.findByIdWithLock(lendId)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));
    
        if (lend.getBackType() != LendList.STATUS_BORROWED && lend.getBackType() != LendList.STATUS_OVERDUE) {
            throw new BusinessException("图书不在借阅中状态，无法归还");
        }
    
        Date now = new Date();
        lend.setBackDate(now);
            
        // 标记为已归还
        lend.setBackType(LendList.STATUS_RETURNED);
    
        // 判断是否逾期并记录罚款
        if (lend.getShouldBackDate() != null && now.after(lend.getShouldBackDate())) {
            // 计算罚款（示例：每天 1 元）
            long diff = now.getTime() - lend.getShouldBackDate().getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            double fineAmount = days * FINE_PER_DAY;
            lend.setFineAmount(fineAmount);
                
            // 可以选择在备注中记录"逾期归还"
            String remarks = lend.getExceptRemarks();
            remarks = (remarks == null || remarks.isEmpty()) ? "逾期归还" : remarks + " (逾期归还)";
            lend.setExceptRemarks(remarks);
        } else {
            // 如果没有逾期，清除罚款金额
            lend.setFineAmount(0.0);
        }
    
        lendListRepository.saveAndFlush(lend);
        syncReaderFineAmount(lend.getReaderId());
    
        // 恢复图书库存
        BookInfo book = bookInfoRepository.findByIdWithLock(lend.getBookId())
                .orElseThrow(() -> new BusinessException("图书不存在"));
        book.setBorrowCount(Math.max(0, (book.getBorrowCount() != null ? book.getBorrowCount() : 0) - 1));
        bookInfoRepository.saveAndFlush(book);
    
        // 更新读者借书数
        ReaderInfo reader = readerInfoService.getById(lend.getReaderId());
        reader.setBorrowCount(Math.max(0, (reader.getBorrowCount() != null ? reader.getBorrowCount() : 0) - 1));
        readerInfoService.update(reader);
    
        // 清除相关缓存
        cacheManager.delete(LEND_STATISTICS_CACHE);
        cacheManager.delete("book:" + lend.getBookId());
        cacheManager.delete("reader:" + lend.getReaderId());
        cacheManager.deletePattern("book:list:*");
        cacheManager.delete("book:statistics");
            
        log.info("图书已归还：lendId={}, status={}", lendId, lend.getBackType());
    }

    @Override
    @Transactional
    public void borrowBooks(Long readerId, List<Long> bookIds, Integer borrowDays) {
        for (Long bookId : bookIds) {
            borrowBook(readerId, bookId, 1, borrowDays, null, null);
        }
    }

    /**
     * 报告图书损坏
     */
    @Override
    @Transactional
    public void reportDamage(Long lendId, String remarks) {
        LendList lend = lendListRepository.findByIdWithLock(lendId)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));
    
        if (lend.getBackType() != LendList.STATUS_BORROWED && lend.getBackType() != LendList.STATUS_OVERDUE) {
            throw new BusinessException("图书不在借阅中状态，无法报告损坏");
        }
    
        // 清除之前的逾期罚款，设置为损坏赔款
        lend.setFineAmount(100.0); // 损坏赔款 100 元
        lend.setBackType(LendList.STATUS_DAMAGED);
        lend.setExceptRemarks(remarks);
        lend.setBackDate(new Date());
        lendListRepository.saveAndFlush(lend);
    
        // 恢复图书库存（因为损坏，库存可能不再增加，但借阅数要减少）
        BookInfo book = bookInfoRepository.findByIdWithLock(lend.getBookId())
                .orElseThrow(() -> new BusinessException("图书不存在"));
        book.setBorrowCount(Math.max(0, (book.getBorrowCount() != null ? book.getBorrowCount() : 0) - 1));
        // 注意：损坏可能需要减少总库存 stock，这里仅减少 borrow_count
        bookInfoRepository.saveAndFlush(book);
    
        // 更新读者借书数和罚款
        ReaderInfo reader = readerInfoService.getById(lend.getReaderId());
        reader.setBorrowCount(Math.max(0, (reader.getBorrowCount() != null ? reader.getBorrowCount() : 0) - 1));
        readerInfoService.update(reader);
        syncReaderFineAmount(lend.getReaderId());
    
        // 清除相关缓存
        cacheManager.delete(LEND_STATISTICS_CACHE);
        cacheManager.delete("book:" + lend.getBookId());
        cacheManager.delete("reader:" + lend.getReaderId());
        cacheManager.deletePattern("book:list:*");
        cacheManager.delete("book:statistics");
    
        log.info("报告图书损坏：lendId={}", lendId);
    }

    /**
     * 统计借阅情况
     */
    @Override
    public Object getBorrowStatistics() {
        refreshOverdueStates();
        Object cached = cacheManager.get(LEND_STATISTICS_CACHE);
        if (cached != null) {
            return cached;
        }

        List<LendList> allLends = lendListRepository.findAll();
        Map<String, Object> statistics = new HashMap<>();

        int totalBorrow = allLends.size();
        long pending = allLends.stream().filter(l -> l.getBackType() == LendList.STATUS_PENDING).count();
        long borrowed = allLends.stream().filter(l -> l.getBackType() == LendList.STATUS_BORROWED || l.getBackType() == LendList.STATUS_OVERDUE).count();
        long returned = allLends.stream().filter(l -> l.getBackType() == LendList.STATUS_RETURNED).count();
        long overdue = allLends.stream().filter(l -> l.getBackType() == LendList.STATUS_OVERDUE).count();
        long damaged = allLends.stream().filter(l -> l.getBackType() == LendList.STATUS_DAMAGED).count();
        long rejected = allLends.stream().filter(l -> l.getBackType() == LendList.STATUS_REJECTED).count();

        statistics.put("totalBorrow", totalBorrow);
        statistics.put("pending", pending);
        statistics.put("borrowed", borrowed);
        statistics.put("returned", returned);
        statistics.put("overdue", overdue);
        statistics.put("damaged", damaged);
        statistics.put("rejected", rejected);

        List<Map<String, Object>> trend = new ArrayList<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DATE, -6);

        for (int i = 0; i < 7; i++) {
            java.util.Date date = cal.getTime();
            String dateStr = sdf.format(date);

            long count = allLends.stream().filter(l -> {
                if (l.getLendDate() == null) return false;
                java.util.Calendar lCal = java.util.Calendar.getInstance();
                lCal.setTime(l.getLendDate());
                return lCal.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                       lCal.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR);
            }).count();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("count", count);
            trend.add(dayData);

            cal.add(java.util.Calendar.DATE, 1);
        }
        statistics.put("trend", trend);

        cacheManager.set(LEND_STATISTICS_CACHE, statistics, CACHE_TIMEOUT);
        return statistics;
    }

    /**
     * 删除借阅记录
     */
    @Override
    @Transactional
    public void deleteLend(Long lendId) {
        if (lendId == null || lendId <= 0) {
            throw new BusinessException("借阅ID不能为空");
        }

        LendList lend = lendListRepository.findByIdWithLock(lendId)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));

        Integer backType = lend.getBackType();
        boolean shouldRollbackBorrow = backType != null &&
                (backType == LendList.STATUS_BORROWED || backType == LendList.STATUS_OVERDUE);

        if (shouldRollbackBorrow) {
            // 回滚图书借出数量
            BookInfo book = bookInfoRepository.findByIdWithLock(lend.getBookId())
                    .orElseThrow(() -> new BusinessException("图书不存在"));
            int borrowed = book.getBorrowCount() != null ? book.getBorrowCount() : 0;
            book.setBorrowCount(Math.max(0, borrowed - 1));
            bookInfoRepository.saveAndFlush(book);

            // 回滚读者借阅数量
            ReaderInfo reader = readerInfoService.getById(lend.getReaderId());
            int readerBorrowed = reader.getBorrowCount() != null ? reader.getBorrowCount() : 0;
            reader.setBorrowCount(Math.max(0, readerBorrowed - 1));
            readerInfoService.update(reader);
        }

        lendListRepository.delete(lend);

        Long readerId = lend.getReaderId();
        Long bookId = lend.getBookId();
        if (readerId != null) {
            syncReaderFineAmount(readerId);
        }

        cacheManager.delete(LEND_STATISTICS_CACHE);
        if (bookId != null) {
            cacheManager.delete("book:" + bookId);
        }
        if (readerId != null) {
            cacheManager.delete("reader:" + readerId);
        }
        cacheManager.deletePattern("book:list:*");
        cacheManager.delete("book:statistics");
    }

    private void refreshOverdueStates() {
        Date now = new Date();
        boolean changed = false;
        Set<Long> affectedReaders = new HashSet<>();
        List<LendList> borrowedList = lendListRepository.findByBackType(LendList.STATUS_BORROWED);
        for (LendList lend : borrowedList) {
            if (lend.getShouldBackDate() != null && now.after(lend.getShouldBackDate())) {
                long diff = now.getTime() - lend.getShouldBackDate().getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                if (days <= 0) days = 1;
                double fine = days * FINE_PER_DAY;
                lend.setBackType(LendList.STATUS_OVERDUE);
                lend.setFineAmount(fine);
                lendListRepository.save(lend);
                if (lend.getReaderId() != null) {
                    affectedReaders.add(lend.getReaderId());
                }
                changed = true;
            }
        }
        List<LendList> overdueList = lendListRepository.findByBackType(LendList.STATUS_OVERDUE);
        for (LendList lend : overdueList) {
            if (lend.getShouldBackDate() != null && now.after(lend.getShouldBackDate())) {
                long diff = now.getTime() - lend.getShouldBackDate().getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                if (days <= 0) days = 1;
                double fine = days * FINE_PER_DAY;
                Double currentFine = lend.getFineAmount();
                if (currentFine != null && Double.compare(currentFine, 0.0) == 0) {
                    continue;
                }
                if (currentFine == null || Double.compare(currentFine, fine) != 0) {
                    lend.setFineAmount(fine);
                    lendListRepository.save(lend);
                    if (lend.getReaderId() != null) {
                        affectedReaders.add(lend.getReaderId());
                    }
                    changed = true;
                }
            }
        }
        if (!affectedReaders.isEmpty()) {
            for (Long readerId : affectedReaders) {
                syncReaderFineAmount(readerId);
            }
        }
        if (changed) {
            cacheManager.delete(LEND_STATISTICS_CACHE);
        }
    }

    private void syncReaderFineAmount(Long readerId) {
        if (readerId == null) return;
        List<LendList> lends = lendListRepository.findByReaderId(readerId);
        double sum = 0.0;
        for (LendList lend : lends) {
            Double fine = lend.getFineAmount();
            if (fine == null || fine <= 0) continue;
            Integer backType = lend.getBackType();
            if (backType == LendList.STATUS_OVERDUE || backType == LendList.STATUS_RETURNED || backType == LendList.STATUS_DAMAGED) {
                sum += fine;
            }
        }
        ReaderInfo current = readerInfoService.getById(readerId);
        double currentFine = current.getFineAmount() != null ? current.getFineAmount() : 0.0;
        if (Double.compare(currentFine, sum) != 0) {
            ReaderInfo update = new ReaderInfo();
            update.setId(readerId);
            update.setFineAmount(sum);
            readerInfoService.update(update);
        }
    }

    @Override
    public void extendBorrow(Long lendId, Integer days) {
        if (lendId == null) {
            throw new BusinessException("借阅记录 ID 不能为空");
        }
        if (days == null || days <= 0) {
            throw new BusinessException("延期天数必须大于 0");
        }
        if (days > 30) {
            throw new BusinessException("单次延期最多 30 天");
        }
    
        LendList lend = lendListRepository.findById(lendId)
                .orElseThrow(() -> new BusinessException("借阅记录不存在"));
    
        // 只能延期借阅中或已逾期的记录
        if (!lend.getBackType().equals(LendList.STATUS_BORROWED) && !lend.getBackType().equals(LendList.STATUS_OVERDUE)) {
            throw new BusinessException("只能延期借阅中或已逾期的记录");
        }
    
        // 计算新的应还日期
        Date currentShouldBackDate = lend.getShouldBackDate();
        if (currentShouldBackDate == null) {
            throw new BusinessException("应还日期为空，无法延期");
        }
    
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentShouldBackDate);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        Date newShouldBackDate = calendar.getTime();
    
        // 更新应还日期
        lend.setShouldBackDate(newShouldBackDate);
    
        // 如果之前是逾期状态，现在延期后变为正常借阅状态
        if (lend.getBackType().equals(LendList.STATUS_OVERDUE)) {
            lend.setBackType(LendList.STATUS_BORROWED);
            lend.setFineAmount(null); // 清除罚款
        }
    
        lendListRepository.save(lend);
    
        // 清除相关缓存
        cacheManager.delete(LEND_STATISTICS_CACHE);
        cacheManager.delete("lend:" + lendId);
        cacheManager.delete("lend:reader:" + lend.getReaderId());
    
        log.info("借阅记录 {} 延期 {} 天，新应还日期：{}", lendId, days, newShouldBackDate);
    }

    @Override
    public void clearAllLends() {
        // 1. 清除所有借阅记录
        lendListRepository.deleteAll();

        // 2. 重置所有读者的借阅计数和罚款
        List<ReaderInfo> readers = readerInfoRepository.findAll();
        for (ReaderInfo reader : readers) {
            reader.setBorrowCount(0);
            reader.setFineAmount(0.0);
            readerInfoRepository.save(reader);
            // 清除读者缓存
            cacheManager.delete("reader:" + reader.getId());
        }

        // 3. 重置所有图书的已借出数量
        List<BookInfo> books = bookInfoRepository.findAll();
        for (BookInfo book : books) {
            book.setBorrowCount(0);
            bookInfoRepository.save(book);
            // 清除图书缓存
            cacheManager.delete("book:" + book.getId());
        }

        // 4. 清除所有相关缓存
        cacheManager.delete(LEND_STATISTICS_CACHE);
        cacheManager.deletePattern("lend:*");

        log.info("所有借阅记录已清除，读者和图书统计已重置");
    }
}
