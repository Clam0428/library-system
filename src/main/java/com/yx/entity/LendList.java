package com.yx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 借阅记录实体类
 */
@Entity
@Table(name = "lend_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LendList implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id")
    private Long bookId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private BookInfo book;

    @Column(name = "reader_id")
    private Long readerId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", insertable = false, updatable = false)
    private ReaderInfo reader;

    @Column(name = "lend_date")
    private Date lendDate;

    /**
     * 应还日期
     */
    @Column(name = "should_back_date")
    private Date shouldBackDate;

    /**
     * 实际还书日期
     */
    @Column(name = "back_date")
    private Date backDate;

    /**
     * 借阅状态：0=待审核(PENDING), 1=已借出(BORROWED), 2=已拒绝(REJECTED), 3=已归还(RETURNED), 4=已逾期(OVERDUE), 5=已损坏(DAMAGED)
     */
    @Column(name = "back_type")
    private Integer backType;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_BORROWED = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_RETURNED = 3;
    public static final int STATUS_OVERDUE = 4;
    public static final int STATUS_DAMAGED = 5;

    /**
     * 异常备注（如损坏情况）
     */
    @Column(name = "except_remarks", columnDefinition = "TEXT")
    private String exceptRemarks;

    /**
     * 罚款金额
     */
    @Column(name = "fine_amount")
    private Double fineAmount;

    /**
     * 审核时间
     */
    @Column(name = "audit_date")
    private Date auditDate;

    /**
     * 审核人ID
     */
    @Column(name = "audit_admin_id")
    private Long auditAdminId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_admin_id", insertable = false, updatable = false)
    private Admin auditAdmin;

    @Transient
    private String readerName;

    @Transient
    private String readerNumber;

    @Transient
    private String readerDepartment;

    @Transient
    private String readerTel;

    @Transient
    private String bookTitle;

    @Transient
    private String auditAdminName;

    @Transient
    private Integer status; // 映射 backType 以适配前端

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if (backType == null) {
            backType = 0;
        }
        if (fineAmount == null) {
            fineAmount = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    public String getReaderName() {
        if (readerName != null) return readerName;
        return reader != null ? reader.getRealName() : null;
    }

    public String getReaderNumber() {
        if (readerNumber != null) return readerNumber;
        return reader != null ? reader.getReaderNumber() : null;
    }

    public String getBookTitle() {
        if (bookTitle != null) return bookTitle;
        return book != null ? book.getName() : null;
    }
}
