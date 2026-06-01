package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.Admin;
import com.yx.entity.Notice;
import com.yx.exception.BusinessException;
import com.yx.service.MessageService;
import com.yx.service.NoticeService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/announcement")
public class AdminAnnouncementController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ApiResponse<Map<String, Object>> send(@RequestBody SendAnnouncementRequest req, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return ApiResponse.fail(401, "请先登录管理员账号");
        }

        if (req == null || req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            return ApiResponse.fail(400, "公告标题不能为空");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            return ApiResponse.fail(400, "公告内容不能为空");
        }

        Notice notice = new Notice();
        notice.setTitle(req.getTitle().trim());
        notice.setContent(req.getContent().trim());
        notice.setAdminId(admin.getId());
        notice.setStatus(1);

        try {
            Notice saved = noticeService.add(notice);
            long sent = messageService.sendSystemMessageToAllReaders(saved.getTitle(), saved.getContent(), saved.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("noticeId", saved.getId());
            data.put("sentCount", sent);
            return ApiResponse.ok(data);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    @Data
    public static class SendAnnouncementRequest {
        private String title;
        private String content;
    }
}

