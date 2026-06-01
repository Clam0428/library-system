package com.yx.repository;

import com.yx.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 管理员数据访问层
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    /**
     * 根据用户名查询管理员
     */
    Optional<Admin> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    Page<Admin> findAllByUsernameContainingAndJobNumber(String username, String jobNumber, Pageable pageable);

    Page<Admin> findAllByUsernameContaining(String username, Pageable pageable);

    Page<Admin> findAllByJobNumber(String jobNumber, Pageable pageable);
}
