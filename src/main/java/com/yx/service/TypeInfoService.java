package com.yx.service;

import com.yx.entity.TypeInfo;
import org.springframework.data.domain.Page;

/**
 * 图书分类服务接口
 */
public interface TypeInfoService {
    /**
     * 分页查询所有分类
     */
    Page<TypeInfo> queryAll(Integer page, Integer limit);

    /**
     * 查询所有分类（不分页）
     */
    java.util.List<TypeInfo> listAll();

    /**
     * 查询单个分类
     */
    TypeInfo getById(Long id);

    /**
     * 添加分类
     */
    TypeInfo add(TypeInfo typeInfo);

    /**
     * 更新分类
     */
    TypeInfo update(TypeInfo typeInfo);

    /**
     * 删除分类
     */
    void delete(Long id);

    /**
     * 批量删除
     */
    void deleteBatch(Long[] ids);

    /**
     * 检查分类名称是否存在
     */
    boolean nameExists(String name);
}
