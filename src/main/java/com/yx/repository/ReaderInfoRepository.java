package com.yx.repository;

import com.yx.entity.ReaderInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 读者数据访问层
 */
@Repository
public interface ReaderInfoRepository extends JpaRepository<ReaderInfo, Long> {
    /**
     * 根据读者号查询
     */
    Optional<ReaderInfo> findByReaderNumber(String readerNumber);

    /**
     * 根据状态分页查询
     */
    Page<ReaderInfo> findByStatus(Integer status, Pageable pageable);

    /**
     * 模糊搜索读者
     */
    @Query("SELECT r FROM ReaderInfo r WHERE r.realName LIKE %:keyword% OR r.readerNumber LIKE %:keyword%")
    Page<ReaderInfo> searchReaders(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 按性别统计读者数量
     */
    @Query("SELECT r.sex as label, COUNT(r) as count FROM ReaderInfo r GROUP BY r.sex")
    java.util.List<java.util.Map<String, Object>> countBySex();

    /**
     * 按专业/部门统计读者数量
     */
    @Query("SELECT r.department as label, COUNT(r) as count FROM ReaderInfo r GROUP BY r.department")
    java.util.List<java.util.Map<String, Object>> countByDepartment();

    @Query("SELECT r.id FROM ReaderInfo r")
    List<Long> findAllIds();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ReaderInfo r SET r.fineAmount = 0.0 WHERE r.id = :readerId")
    int clearFineAmount(@Param("readerId") Long readerId);
}
