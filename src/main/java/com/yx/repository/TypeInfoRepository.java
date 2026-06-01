package com.yx.repository;

import com.yx.entity.TypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 图书分类数据访问层
 */
@Repository
public interface TypeInfoRepository extends JpaRepository<TypeInfo, Long> {
    /**
     * 根据名称查询
     */
    Optional<TypeInfo> findByName(String name);

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);
}
