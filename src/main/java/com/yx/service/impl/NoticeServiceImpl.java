package com.yx.service.impl;

import com.yx.entity.Notice;
import com.yx.exception.BusinessException;
import com.yx.repository.MessageRepository;
import com.yx.repository.NoticeRepository;
import com.yx.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 公告服务实现
 */
@Slf4j
@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 分页查询所有公告
     */
    @Override
    public Page<Notice> queryAll(Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        return noticeRepository.findAll(PageRequest.of(page - 1, limit));
    }

    /**
     * 查询已发布的公告
     */
    @Override
    public Page<Notice> queryPublished(Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        return noticeRepository.findByStatus(1, PageRequest.of(page - 1, limit));
    }

    /**
     * 查询单个公告
     */
    @Override
    public Notice getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("公告ID不能为空");
        }
        Optional<Notice> noticeOpt = noticeRepository.findById(id);
        if (!noticeOpt.isPresent()) {
            throw new BusinessException("公告不存在");
        }
        return noticeOpt.get();
    }

    /**
     * 添加公告
     */
    @Override
    public Notice add(Notice notice) {
        if (notice == null || notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new BusinessException("公告标题不能为空");
        }

        log.info("添加新公告: {}", notice.getTitle());
        return noticeRepository.save(notice);
    }

    /**
     * 更新公告
     */
    @Override
    public Notice update(Notice notice) {
        if (notice == null || notice.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }

        if (!noticeRepository.existsById(notice.getId())) {
            throw new BusinessException("公告不存在");
        }

        log.info("更新公告: id={}", notice.getId());
        return noticeRepository.save(notice);
    }

    /**
     * 删除公告
     */
    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("公告ID不能为空");
        }

        if (!noticeRepository.existsById(id)) {
            throw new BusinessException("公告不存在");
        }

        // 先删除关联的站内消息
        messageRepository.deleteByNoticeId(id);
        // 再删除公告本身
        noticeRepository.deleteById(id);
        log.info("删除公告及其关联消息: id={}", id);
    }

    /**
     * 发布公告
     */
    @Override
    public void publish(Long id) {
        Notice notice = getById(id);
        notice.setStatus(1);
        noticeRepository.save(notice);
        log.info("发布公告: id={}", id);
    }

    /**
     * 下线公告
     */
    @Override
    public void unpublish(Long id) {
        Notice notice = getById(id);
        notice.setStatus(0);
        noticeRepository.save(notice);
        log.info("下线公告: id={}", id);
    }
}
