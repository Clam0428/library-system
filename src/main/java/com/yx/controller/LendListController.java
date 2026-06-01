package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.LendList;
import com.yx.exception.BusinessException;
import com.yx.service.LendListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借阅管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/lend")
public class LendListController {

    @Autowired
    private LendListService lendListService;

    /**
     * 分页查询所有借阅记录
     */
    @GetMapping("/list")
    public ApiResponse<List<LendList>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer limit,
                                            @RequestParam(required = false) String readerName,
                                            @RequestParam(required = false) String bookName,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date startDate,
                                            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date endDate) {
        try {
            Page<LendList> result = lendListService.queryAll(page, limit, readerName, bookName, status, startDate, endDate);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询读者的借阅记录
     */
    @GetMapping("/listByReader/{readerId}")
    public ApiResponse<List<LendList>> listByReader(@PathVariable Long readerId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<LendList> result = lendListService.queryByReader(readerId, page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询图书的借阅记录
     */
    @GetMapping("/listByBook/{bookId}")
    public ApiResponse<List<LendList>> listByBook(@PathVariable Long bookId) {
        try {
            List<LendList> result = lendListService.queryByBook(bookId);
            return ApiResponse.ok(result);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询未还的借阅记录
     */
    @GetMapping("/unreturned/{readerId}")
    public ApiResponse<List<LendList>> queryUnreturned(@PathVariable Long readerId) {
        try {
            List<LendList> result = lendListService.queryUnreturned(readerId);
            return ApiResponse.ok(result);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 查询逾期借阅
     */
    @GetMapping("/overdue")
    public ApiResponse<List<LendList>> queryOverdue(@RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<LendList> result = lendListService.queryOverdue(page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取借阅详情
     */
    @GetMapping("/{id}")
    public ApiResponse<LendList> getById(@PathVariable Long id) {
        try {
            LendList lend = lendListService.getById(id);
            return ApiResponse.ok(lend);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    @GetMapping("/detailForReader/{id}")
    public ApiResponse<LendList> getDetailForReader(@PathVariable Long id, javax.servlet.http.HttpSession session) {
        try {
            com.yx.entity.ReaderInfo reader = (com.yx.entity.ReaderInfo) session.getAttribute("reader");
            if (reader == null) {
                return ApiResponse.fail(401, "未登录，请先登录");
            }
            LendList lend = lendListService.getById(id);
            if (lend == null) {
                return ApiResponse.fail(404, "借阅记录不存在");
            }
            if (lend.getReaderId() == null || !lend.getReaderId().equals(reader.getId())) {
                return ApiResponse.fail(403, "无权查看该借阅记录");
            }
            return ApiResponse.ok(lend);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除借阅记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            lendListService.deleteLend(id);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 借书
     */
    @PostMapping("/borrow")
    public ApiResponse<Void> borrowBook(@RequestParam Long readerId,
                                        @RequestParam Long bookId,
                                        @RequestParam(defaultValue = "1") Integer count,
                                        @RequestParam(required = false) Integer borrowDays,
                                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date lendDate,
                                        @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") java.util.Date shouldBackDate) {
        try {
            lendListService.borrowBook(readerId, bookId, count, borrowDays, lendDate, shouldBackDate);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 批量借书
     */
    @PostMapping("/borrowBatch")
    public ApiResponse<Void> borrowBooks(@RequestParam Long readerId,
                                         @RequestBody List<Long> bookIds,
                                         @RequestParam(required = false) Integer borrowDays) {
        try {
            lendListService.borrowBooks(readerId, bookIds, borrowDays);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 管理员审核借书申请
     * @param lendId 借阅记录ID
     * @param status 1=同意, 2=拒绝
     */
    @PostMapping("/audit/{lendId}")
    public ApiResponse<Void> auditBorrow(@PathVariable Long lendId, @RequestParam Integer status, 
                                         @RequestParam(required = false) String remarks,
                                         javax.servlet.http.HttpSession session) {
        try {
            com.yx.entity.Admin admin = (com.yx.entity.Admin) session.getAttribute("admin");
            if (admin == null) {
                return ApiResponse.fail(401, "请先登录管理员账号");
            }
            lendListService.auditBorrow(lendId, status, admin.getId(), remarks);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 还书
     */
    @PostMapping("/return/{lendId}")
    public ApiResponse<Void> returnBook(@PathVariable Long lendId) {
        try {
            lendListService.returnBook(lendId);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 报告图书损坏
     */
    @PostMapping("/reportDamage/{lendId}")
    public ApiResponse<Void> reportDamage(@PathVariable Long lendId,
                                          @RequestParam(required = false) String remarks) {
        try {
            lendListService.reportDamage(lendId, remarks);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取借阅统计
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getBorrowStatistics() {
        try {
            Object statistics = lendListService.getBorrowStatistics();
            return ApiResponse.ok(statistics);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 延期借阅
     */
    @PostMapping("/extend/{lendId}")
    public ApiResponse<Void> extendBorrow(@PathVariable Long lendId, @RequestParam Integer days) {
        try {
            lendListService.extendBorrow(lendId, days);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 清除所有借阅记录（管理员专用）
     */
    @PostMapping("/clearAll")
    public ApiResponse<Void> clearAllLends(javax.servlet.http.HttpSession session) {
        try {
            com.yx.entity.Admin admin = (com.yx.entity.Admin) session.getAttribute("admin");
            if (admin == null) {
                return ApiResponse.fail(401, "请先登录管理员账号");
            }
            lendListService.clearAllLends();
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }
}
