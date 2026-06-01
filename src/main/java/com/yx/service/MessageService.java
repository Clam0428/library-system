package com.yx.service;

import com.yx.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 发送系统消息
     */
    void sendSystemMessage(Long readerId, String title, String content);

    long sendSystemMessageToAllReaders(String title, String content, Long noticeId);
    
    /**
     * 获取读者的消息列表
     */
    Page<Message> getReaderMessages(Long readerId, Pageable pageable);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);
    
    /**
     * 获取未读消息数
     */
    long getUnreadCount(Long readerId);
    
    /**
     * 删除消息
     */
    void deleteMessage(Long messageId);
}
