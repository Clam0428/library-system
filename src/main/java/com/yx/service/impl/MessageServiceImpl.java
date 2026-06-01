package com.yx.service.impl;

import com.yx.entity.Message;
import com.yx.repository.MessageRepository;
import com.yx.repository.ReaderInfoRepository;
import com.yx.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReaderInfoRepository readerInfoRepository;

    @Override
    @Transactional
    public void sendSystemMessage(Long readerId, String title, String content) {
        Message message = new Message();
        message.setReaderId(readerId);
        message.setTitle(title);
        message.setContent(content);
        message.setStatus(0); // 未读
        message.setCreateTime(new Date());
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public long sendSystemMessageToAllReaders(String title, String content, Long noticeId) {
        List<Long> readerIds = readerInfoRepository.findAllIds();
        if (readerIds == null || readerIds.isEmpty()) {
            return 0;
        }

        int batchSize = 500;
        long sent = 0;
        Date now = new Date();

        for (int i = 0; i < readerIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, readerIds.size());
            List<Long> part = readerIds.subList(i, end);
            List<Message> batch = new ArrayList<>(part.size());
            for (Long readerId : part) {
                Message message = new Message();
                message.setReaderId(readerId);
                message.setNoticeId(noticeId);
                message.setTitle(title);
                message.setContent(content);
                message.setStatus(0);
                message.setCreateTime(now);
                batch.add(message);
            }
            messageRepository.saveAll(batch);
            sent += batch.size();
        }

        return sent;
    }

    @Override
    public Page<Message> getReaderMessages(Long readerId, Pageable pageable) {
        return messageRepository.findByReaderIdOrderByCreateTimeDesc(readerId, pageable);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setStatus(1); // 已读
            messageRepository.save(m);
        });
    }

    @Override
    public long getUnreadCount(Long readerId) {
        return messageRepository.countByReaderIdAndStatus(readerId, 0);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }
}
