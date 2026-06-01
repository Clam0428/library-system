package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.TypeInfo;
import com.yx.exception.BusinessException;
import com.yx.service.TypeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/type")
public class TypeInfoController {

    @Autowired
    private TypeInfoService typeInfoService;

    /**
     * 分页查询所有分类
     */
    @GetMapping("/list")
    public ApiResponse<List<TypeInfo>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<TypeInfo> result = typeInfoService.queryAll(page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询所有分类（不分页）
     */
    @GetMapping("/listAll")
    public ApiResponse<List<TypeInfo>> listAll() {
        try {
            List<TypeInfo> result = typeInfoService.listAll();
            return ApiResponse.ok(result);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TypeInfo> getById(@PathVariable Long id) {
        try {
            TypeInfo type = typeInfoService.getById(id);
            return ApiResponse.ok(type);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 添加分类
     */
    @PostMapping("/add")
    public ApiResponse<TypeInfo> add(@RequestBody TypeInfo typeInfo) {
        try {
            TypeInfo saved = typeInfoService.add(typeInfo);
            return ApiResponse.ok(saved);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改分类
     */
    @PostMapping("/update")
    public ApiResponse<TypeInfo> update(@RequestBody TypeInfo typeInfo) {
        try {
            TypeInfo updated = typeInfoService.update(typeInfo);
            return ApiResponse.ok(updated);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            typeInfoService.delete(id);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 批量删除
     */
    @PostMapping("/deleteBatch")
    public ApiResponse<Void> deleteBatch(@RequestBody Long[] ids) {
        try {
            typeInfoService.deleteBatch(ids);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }
}
