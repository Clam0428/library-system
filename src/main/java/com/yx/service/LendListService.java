package com.yx.service;

import com.yx.entity.LendList;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;

/**
 * 借阅管理服务接口
 */
public interface LendListService {
    /**
     * 分页查询所有借阅记录
     */
    Page<LendList> queryAll(Integer page, Integer limit);

    /**
     * 条件分页查询
     */
    Page<LendList> queryAll(Integer page, Integer limit, String readerName, String bookName, Integer status,
                            java.util.Date startDate, java.util.Date endDate);

    /**
     * 查询读者的借阅记录
     */
    Page<LendList> queryByReader(Long readerId, Integer page, Integer limit);

    /**
     * 查询图书的借阅记录
     */
    List<LendList> queryByBook(Long bookId);

    /**
     * 查询未还的借阅记录
     */
    List<LendList> queryUnreturned(Long readerId);

    /**
     * 查询逾期借阅
     */
    Page<LendList> queryOverdue(Integer page, Integer limit);

    /**
     * 获取单个借阅记录
     */
    LendList getById(Long id);

    /**
     * 借书
     */
    void borrowBook(Long readerId, Long bookId, Integer count, Integer borrowDays, Date lendDate, Date shouldBackDate);

    /**
     * 批量借书
     */
    void borrowBooks(Long readerId, List<Long> bookIds, Integer borrowDays);

    /**
     * 还书
     */
    void returnBook(Long lendId);

    /**
     * 审核借书申请
     * @param lendId 借阅记录ID
     * @param status 1=同意(BORROWED), 2=拒绝(REJECTED)
     * @param adminId 管理员ID
     */
    void auditBorrow(Long lendId, Integer status, Long adminId, String remarks);

    /**
     * 报告图书损坏
     */
    void reportDamage(Long lendId, String remarks);

    void deleteLend(Long lendId);

    /**
     * 统计借阅情况
     */
    Object getBorrowStatistics();

    /**
     * 延期借阅
     * @param lendId 借阅记录ID
     * @param days 延期天数
     */
    void extendBorrow(Long lendId, Integer days);

    /**
     * 清除所有借阅记录（管理员专用）
     */
    void clearAllLends();
}
