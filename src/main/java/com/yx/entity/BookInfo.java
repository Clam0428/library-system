package com.yx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 图书实体类
 */
@Entity
@Table(name = "book_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String author;

    private String publish;

    @Column(name = "isbn")
    private String location;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    private String language;

    private BigDecimal price;

    @Column(name = "publish_date")
    private Date publishDate;

    @Column(name = "type_id")
    private Long typeId;

    @Transient
    private String typeName;

    /**
     * 是否需要审核：0=不需要, 1=需要
     */
    @Column(name = "is_review_required")
    private Integer isReviewRequired;

    /**
     * 0=正常可用，1=已借出，2=禁用
     */
    private Integer status;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 已借出数量
     */
    @Column(name = "borrow_count")
    private Integer borrowCount;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if (status == null) {
            status = 0;
        }
        if (borrowCount == null) {
            borrowCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
}
