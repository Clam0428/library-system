package com.yx.task;

import com.yx.entity.LendList;
import com.yx.entity.ReaderInfo;
import com.yx.repository.LendListRepository;
import com.yx.service.MessageService;
import com.yx.service.ReaderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 逾期检查定时任务
 */
@Slf4j
@Component
public class OverdueCheckTask {

    @Autowired
    private LendListRepository lendListRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ReaderInfoService readerInfoService;

    private static final double FINE_PER_DAY = 1.0; // 每天罚款1元

    /**
     * 每天凌晨1点执行逾期检查
     * 0 0 1 * * ?
     * 为了演示，我们也可以设置得频繁一点，比如每小时执行一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkOverdue() {
        log.info("开始执行逾期检查任务...");
        Date now = new Date();
        Set<Long> affectedReaders = new HashSet<>();
        
        // 查询所有“已借出”状态且超过“应还日期”的记录
        List<LendList> borrowedList = lendListRepository.findByBackType(LendList.STATUS_BORROWED);
        
        int count = 0;
        for (LendList lend : borrowedList) {
            if (lend.getShouldBackDate() != null && now.after(lend.getShouldBackDate())) {
                // 1. 更新状态为逾期
                lend.setBackType(LendList.STATUS_OVERDUE);
                
                // 2. 计算当前罚款金额
                long diff = now.getTime() - lend.getShouldBackDate().getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                if (days <= 0) days = 1; // 哪怕刚过零点也算一天
                lend.setFineAmount(days * FINE_PER_DAY);
                
                lendListRepository.save(lend);
                if (lend.getReaderId() != null) {
                    affectedReaders.add(lend.getReaderId());
                }
                
                // 3. 发送站内信提醒
                sendMessage(lend, days);
                
                count++;
            }
        }
        
        // 对于已经是“逾期”状态的，更新罚款金额
        List<LendList> overdueList = lendListRepository.findByBackType(LendList.STATUS_OVERDUE);
        for (LendList lend : overdueList) {
            if (lend.getShouldBackDate() != null && now.after(lend.getShouldBackDate())) {
                long diff = now.getTime() - lend.getShouldBackDate().getTime();
                long days = diff / (1000 * 60 * 60 * 24);
                if (days <= 0) days = 1;
                lend.setFineAmount(days * FINE_PER_DAY);
                lendListRepository.save(lend);
                if (lend.getReaderId() != null) {
                    affectedReaders.add(lend.getReaderId());
                }
            }
        }
        if (!affectedReaders.isEmpty()) {
            for (Long readerId : affectedReaders) {
                syncReaderFineAmount(readerId);
            }
        }

        log.info("逾期检查任务完成，共处理 {} 条新逾期记录", count);
    }

    private void sendMessage(LendList lend, long days) {
        try {
            String bookName = lend.getBookTitle();
            if (bookName == null && lend.getBook() != null) bookName = lend.getBook().getName();
            if (bookName == null) bookName = "图书(ID:" + lend.getBookId() + ")";
            
            messageService.sendSystemMessage(lend.getReaderId(), "图书逾期提醒", 
                String.format("您借阅的《%s》已逾期 %d 天，请尽快归还。当前逾期罚款：%.2f元。逾期未还将影响您的继续借阅。", 
                    bookName, days, lend.getFineAmount()));
        } catch (Exception e) {
            log.error("发送逾期提醒消息失败: lendId={}", lend.getId(), e);
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
}
