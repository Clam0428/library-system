package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.Notice;
import com.yx.exception.BusinessException;
import com.yx.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询所有公告
     */
    @GetMapping("/list")
    public ApiResponse<List<Notice>> list(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<Notice> result = noticeService.queryAll(page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询已发布的公告
     */
    @GetMapping("/published")
    public ApiResponse<List<Notice>> published(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<Notice> result = noticeService.queryPublished(page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Notice> getById(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getById(id);
            return ApiResponse.ok(notice);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 添加公告
     */
    @PostMapping("/add")
    public ApiResponse<Notice> add(@RequestBody Notice notice) {
        try {
            Notice saved = noticeService.add(notice);
            return ApiResponse.ok(saved);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改公告
     */
    @PostMapping("/update")
    public ApiResponse<Notice> update(@RequestBody Notice notice) {
        try {
            Notice updated = noticeService.update(notice);
            return ApiResponse.ok(updated);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            noticeService.delete(id);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 发布公告
     */
    @PostMapping("/publish/{id}")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        try {
            noticeService.publish(id);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 下线公告
     */
    @PostMapping("/unpublish/{id}")
    public ApiResponse<Void> unpublish(@PathVariable Long id) {
        try {
            noticeService.unpublish(id);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }
}
