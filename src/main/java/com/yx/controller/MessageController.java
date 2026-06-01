package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.Message;
import com.yx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list/{readerId}")
    public ApiResponse<List<Message>> list(@PathVariable Long readerId,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer limit) {
        Page<Message> result = messageService.getReaderMessages(readerId, PageRequest.of(page - 1, limit));
        return ApiResponse.ok(result.getTotalElements(), result.getContent());
    }

    @PostMapping("/read/{id}")
    public ApiResponse<Void> read(@PathVariable Long id) {
        messageService.markAsRead(id);
        return ApiResponse.ok();
    }

    @GetMapping("/unread/count/{readerId}")
    public ApiResponse<Long> unreadCount(@PathVariable Long readerId) {
        long count = messageService.getUnreadCount(readerId);
        return ApiResponse.ok(count);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ApiResponse.ok();
    }
}
