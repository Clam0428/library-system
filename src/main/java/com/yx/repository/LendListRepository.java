package com.yx.repository;

import com.yx.entity.LendList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Lock;
import javax.persistence.LockModeType;
import java.util.Optional;

/**
 * 借阅记录数据访问层
 */
@Repository
public interface LendListRepository extends JpaRepository<LendList, Long>, JpaSpecificationExecutor<LendList> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LendList l where l.id = :id")
    Optional<LendList> findByIdWithLock(@Param("id") Long id);
    /**
     * 查询读者未还的借阅记录
     */
    List<LendList> findByReaderIdAndBackType(Long readerId, Integer backType);

    /**
     * 查询图书的借阅记录
     */
    List<LendList> findByBookId(Long bookId);

    /**
     * 查询读者的所有借阅记录（分页）
     */
    Page<LendList> findByReaderId(Long readerId, Pageable pageable);

    List<LendList> findByReaderId(Long readerId);

    /**
     * 查询逾期未还的借阅记录
     */
    @Query("SELECT l FROM LendList l WHERE l.backType = 1 AND l.shouldBackDate < CURRENT_TIMESTAMP")
    Page<LendList> findOverdueBooks(Pageable pageable);

    /**
     * 查询特定状态的借阅记录
     */
    List<LendList> findByBackType(Integer backType);

    /**
     * 查询特定状态的借阅记录（分页）
     */
    Page<LendList> findByBackType(Integer backType, Pageable pageable);

    /**
     * 按性别统计借阅量
     */
    @Query("SELECT (CASE WHEN r.sex = 2 THEN 2 ELSE 1 END) as label, COUNT(l) as count FROM LendList l JOIN l.reader r GROUP BY (CASE WHEN r.sex = 2 THEN 2 ELSE 1 END)")
    List<java.util.Map<String, Object>> countByReaderSex();

    /**
     * 按部门/专业统计借阅量
     */
    @Query("SELECT r.department as label, COUNT(l) as count FROM LendList l JOIN l.reader r GROUP BY r.department")
    List<java.util.Map<String, Object>> countByReaderDepartment();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE LendList l SET l.fineAmount = 0.0 WHERE l.readerId = :readerId AND l.fineAmount > 0")
    int clearFineByReaderId(@Param("readerId") Long readerId);
}
