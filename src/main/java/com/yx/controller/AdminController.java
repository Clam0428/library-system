package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.Admin;
import com.yx.exception.BusinessException;
import com.yx.service.AdminService;
import com.yx.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private com.yx.repository.AdminRepository adminRepository;

    @Autowired
    private com.yx.repository.BookInfoRepository bookInfoRepository;

    @Autowired
    private com.yx.repository.ReaderInfoRepository readerInfoRepository;

    @Autowired
    private com.yx.service.LendListService lendListService;

    @Autowired
    private com.yx.repository.LendListRepository lendListRepository;

    @Autowired
    private com.yx.service.BookInfoService bookInfoService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ApiResponse<Admin> login(@RequestParam String username, @RequestParam String password, HttpSession session) {
        try {
            Admin admin = adminService.login(username, password);
            session.setAttribute("admin", admin);
            session.setAttribute("userType", "admin");
            return ApiResponse.ok(admin);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/currentUser")
    public ApiResponse<Admin> getCurrentUser(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return ApiResponse.fail(401, "未登录");
        }
        Optional<Admin> dbAdmin = adminRepository.findById(admin.getId());
        if (dbAdmin.isPresent()) {
            session.setAttribute("admin", dbAdmin.get());
            return ApiResponse.ok(dbAdmin.get());
        }
        return ApiResponse.ok(admin);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.ok();
    }

    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    public ApiResponse<Void> updatePassword(@RequestParam Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
        try {
            adminService.updatePassword(id, oldPassword, newPassword);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/updateProfile")
    public ApiResponse<Admin> updateProfile(@RequestBody Admin payload, HttpSession session) {
        try {
            Admin sessionAdmin = (Admin) session.getAttribute("admin");
            if (sessionAdmin == null) {
                return ApiResponse.fail(401, "未登录");
            }
            if (payload == null) {
                return ApiResponse.fail(400, "参数不能为空");
            }
            payload.setId(sessionAdmin.getId());
            Admin updated = adminService.updateProfile(payload);
            session.setAttribute("admin", updated);
            return ApiResponse.ok(updated);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取管理员信息
     */
    @GetMapping("/{id}")
    public ApiResponse<Admin> getAdmin(@PathVariable Long id) {
        try {
            Optional<Admin> adminOpt = adminService.getById(id);
            if (!adminOpt.isPresent()) {
                return ApiResponse.fail("管理员不存在");
            }
            return ApiResponse.ok(adminOpt.get());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 添加管理员
     */
    @PostMapping("/add")
    public ApiResponse<Admin> addAdmin(@RequestBody Admin admin) {
        try {
            Admin saved = adminService.add(admin);
            return ApiResponse.ok(saved);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 管理后台统计数据（图书数、读者数、借阅统计等）
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        try {
            Map<String, Object> result = new HashMap<>();
            long totalBooks = bookInfoRepository.sumStock();
            long totalReaders = readerInfoRepository.count();
            Object borrowStats = lendListService.getBorrowStatistics();
            Object typeStats = bookInfoService.getTypeStatistics();

            List<Map<String, Object>> genderStats = lendListRepository.countByReaderSex();
            List<Map<String, Object>> departmentStats = lendListRepository.countByReaderDepartment();

            result.put("totalBooks", totalBooks);
            result.put("totalReaders", totalReaders);
            result.put("borrowStats", borrowStats);
            result.put("typeStats", typeStats);
            result.put("genderStats", genderStats);
            result.put("departmentStats", departmentStats);

            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return ApiResponse.fail(500, "获取统计数据失败");
        }
    }

    /**
     * 获取最近借阅记录（用于仪表盘显示）
     */
    @GetMapping("/recentLends")
    public ApiResponse<List<com.yx.entity.LendList>> recentLends() {
        try {
            // 获取最近10条借阅记录
            Page<com.yx.entity.LendList> page = lendListService.queryAll(1, 10);
            return ApiResponse.ok(page.getContent());
        } catch (Exception e) {
            log.error("获取最近借阅记录失败", e);
            return ApiResponse.fail(500, "获取数据失败");
        }
    }

    /**
     * 管理员列表（分页、可按用户名与类型搜索）
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "15") int limit,
                                                 @RequestParam(required = false) String username,
                                                 @RequestParam(required = false) String jobNumber) {
        try {
            Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
            Page<Admin> p;
            if (username != null && !username.trim().isEmpty() && jobNumber != null && !jobNumber.trim().isEmpty()) {
                p = adminRepository.findAllByUsernameContainingAndJobNumber(username.trim(), jobNumber.trim(), pageable);
            } else if (username != null && !username.trim().isEmpty()) {
                p = adminRepository.findAllByUsernameContaining(username.trim(), pageable);
            } else if (jobNumber != null && !jobNumber.trim().isEmpty()) {
                p = adminRepository.findAllByJobNumber(jobNumber.trim(), pageable);
            } else {
                p = adminRepository.findAll(pageable);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", p.getTotalElements());
            result.put("data", p.getContent());
            return ApiResponse.ok(result);
        } catch (Exception e) {
            log.error("查询管理员列表失败", e);
            return ApiResponse.fail(500, "查询管理员列表失败");
        }
    }

    /**
     * 删除管理员（支持按逗号分隔的ids批量删除）
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestParam String ids) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return ApiResponse.fail(400, "未提供要删除的id");
            }
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            // 使用 repository 批量删除
            adminRepository.deleteAllById(idList);
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("删除管理员失败", e);
            return ApiResponse.fail(500, "删除管理员失败");
        }
    }
}
