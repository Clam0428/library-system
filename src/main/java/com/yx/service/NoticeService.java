package com.yx.service;

import com.yx.entity.Notice;
import org.springframework.data.domain.Page;

/**
 * 公告服务接口
 */
public interface NoticeService {
    /**
     * 分页查询所有公告
     */
    Page<Notice> queryAll(Integer page, Integer limit);

    /**
     * 查询已发布的公告
     */
    Page<Notice> queryPublished(Integer page, Integer limit);

    /**
     * 查询单个公告
     */
    Notice getById(Long id);

    /**
     * 添加公告
     */
    Notice add(Notice notice);

    /**
     * 更新公告
     */
    Notice update(Notice notice);

    /**
     * 删除公告
     */
    void delete(Long id);

    /**
     * 发布公告
     */
    void publish(Long id);

    /**
     * 下线公告
     */
    void unpublish(Long id);
}
