package com.yx.service.impl;

import com.yx.entity.TypeInfo;
import com.yx.exception.BusinessException;
import com.yx.repository.TypeInfoRepository;
import com.yx.service.TypeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 图书分类服务实现
 */
@Slf4j
@Service
@Transactional
public class TypeInfoServiceImpl implements TypeInfoService {

    @Autowired
    private TypeInfoRepository typeInfoRepository;

    /**
     * 分页查询所有分类
     */
    @Override
    public Page<TypeInfo> queryAll(Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        return typeInfoRepository.findAll(PageRequest.of(page - 1, limit));
    }

    @Override
    public List<TypeInfo> listAll() {
        return typeInfoRepository.findAll();
    }

    @Override
    public TypeInfo getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("分类ID不能为空");
        }
        Optional<TypeInfo> typeOpt = typeInfoRepository.findById(id);
        if (!typeOpt.isPresent()) {
            throw new BusinessException("分类不存在");
        }
        return typeOpt.get();
    }

    /**
     * 添加分类
     */
    @Override
    public TypeInfo add(TypeInfo typeInfo) {
        if (typeInfo == null || typeInfo.getName() == null || typeInfo.getName().trim().isEmpty()) {
            throw new BusinessException("分类名称不能为空");
        }

        if (typeInfoRepository.existsByName(typeInfo.getName())) {
            throw new BusinessException("分类名称已存在");
        }

        log.info("添加新分类: {}", typeInfo.getName());
        return typeInfoRepository.save(typeInfo);
    }

    /**
     * 更新分类
     */
    @Override
    public TypeInfo update(TypeInfo typeInfo) {
        if (typeInfo == null || typeInfo.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }

        if (!typeInfoRepository.existsById(typeInfo.getId())) {
            throw new BusinessException("分类不存在");
        }

        log.info("更新分类: id={}", typeInfo.getId());
        return typeInfoRepository.save(typeInfo);
    }

    /**
     * 删除分类
     */
    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("分类ID不能为空");
        }

        if (!typeInfoRepository.existsById(id)) {
            throw new BusinessException("分类不存在");
        }

        log.info("删除分类: id={}", id);
        typeInfoRepository.deleteById(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("删除ID列表不能为空");
        }

        log.info("批量删除分类: count={}", ids.length);
        for (Long id : ids) {
            typeInfoRepository.deleteById(id);
        }
    }

    /**
     * 检查分类名称是否存在
     */
    @Override
    public boolean nameExists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return typeInfoRepository.existsByName(name);
    }
}
