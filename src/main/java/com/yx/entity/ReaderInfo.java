package com.yx.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 读者实体类
 */
@Entity
@Table(name = "reader_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReaderInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "department")
    private String department;

    /**
     * 1=男, 2=女
     */
    private Integer sex;

    private Date birthday;

    private String address;

    private String tel;

    private String email;

    @Column(name = "register_date")
    private Date registerDate;

    @Column(name = "reader_number", unique = true)
    private String readerNumber;

    /**
     * 0=正常, 1=禁用
     */
    private Integer status;

    /**
     * 已借图书数
     */
    @Column(name = "borrow_count")
    private Integer borrowCount;

    /**
     * 罚款金额
     */
    @Column(name = "fine_amount")
    private Double fineAmount;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 头像路径
     */
    private String avatar;

    @PrePersist
    protected void onCreate() {
        registerDate = new Date();
        createTime = new Date();
        updateTime = new Date();
        if (status == null) {
            status = 0;
        }
        if (sex == null) {
            sex = 1;
        }
        if (borrowCount == null) {
            borrowCount = 0;
        }
        if (fineAmount == null) {
            fineAmount = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
}
