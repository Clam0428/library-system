package com.yx.repository;

import com.yx.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByReaderIdOrderByCreateTimeDesc(Long readerId, Pageable pageable);
    long countByReaderIdAndStatus(Long readerId, Integer status);
    void deleteByNoticeId(Long noticeId);
}
