package com.yx.repository;

import com.yx.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 公告数据访问层
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    /**
     * 查询已发布的公告（分页）
     */
    Page<Notice> findByStatus(Integer status, Pageable pageable);
}
