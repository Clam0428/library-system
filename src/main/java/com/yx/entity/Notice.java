package com.yx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 公告实体类
 */
@Entity
@Table(name = "notice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "admin_id")
    private Long adminId;

    /**
     * 0=草稿, 1=已发布
     */
    private Integer status;

    @Column(name = "update_time")
    private Date updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if (status == null) {
            status = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
}
