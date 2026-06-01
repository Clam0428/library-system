package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.BookInfo;
import com.yx.exception.BusinessException;
import com.yx.service.BookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图书管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    /**
     * 分页查询所有图书
     */
    @GetMapping("/list")
    public ApiResponse<List<BookInfo>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<BookInfo> result = bookInfoService.queryAll(page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 按类型查询
     */
    @GetMapping("/listByType/{typeId}")
    public ApiResponse<List<BookInfo>> listByType(@PathVariable Long typeId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<BookInfo> result = bookInfoService.queryByType(typeId, page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 搜索图书
     */
    @GetMapping("/search")
    public ApiResponse<List<BookInfo>> search(@RequestParam String keyword,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<BookInfo> result = bookInfoService.searchBooks(keyword, page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取图书详情
     */
    @GetMapping("/{id}")
    public ApiResponse<BookInfo> getById(@PathVariable Long id) {
        try {
            BookInfo book = bookInfoService.getById(id);
            return ApiResponse.ok(book);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 添加图书
     */
    @PostMapping("/add")
    public ApiResponse<BookInfo> add(@RequestBody BookInfo bookInfo) {
        try {
            BookInfo saved = bookInfoService.add(bookInfo);
            return ApiResponse.ok(saved);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改图书
     */
    @PostMapping("/update")
    public ApiResponse<BookInfo> update(@RequestBody BookInfo bookInfo) {
        try {
            BookInfo updated = bookInfoService.update(bookInfo);
            return ApiResponse.ok(updated);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            bookInfoService.delete(id);
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
            bookInfoService.deleteBatch(ids);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getTypeStatistics() {
        try {
            Object statistics = bookInfoService.getTypeStatistics();
            return ApiResponse.ok(statistics);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }
}
