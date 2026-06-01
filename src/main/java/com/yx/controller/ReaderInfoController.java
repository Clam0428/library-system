package com.yx.controller;

import com.yx.dto.ApiResponse;
import com.yx.entity.ReaderInfo;
import com.yx.exception.BusinessException;
import com.yx.service.ReaderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 读者管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/reader")
public class ReaderInfoController {

    @Autowired
    private ReaderInfoService readerInfoService;

    /**
     * 读者登录
     */
    @PostMapping("/login")
    public ApiResponse<ReaderInfo> login(@RequestParam String readerNumber, @RequestParam String password, HttpSession session) {
        try {
            ReaderInfo reader = readerInfoService.login(readerNumber, password);
            session.setAttribute("reader", reader);
            session.setAttribute("userType", "reader");
            return ApiResponse.ok(reader);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());

        }
    }

    /**
     * 读者注册
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ReaderInfo> registerJson(@RequestBody ReaderInfo readerInfo) {
        try {
            ReaderInfo saved = readerInfoService.add(readerInfo);
            return ApiResponse.ok(saved);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ApiResponse<ReaderInfo> registerForm(ReaderInfo readerInfo) {
        try {
            ReaderInfo saved = readerInfoService.add(readerInfo);
            return ApiResponse.ok(saved);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取当前登录读者信息
     */
    @GetMapping("/currentUser")
    public ApiResponse<ReaderInfo> getCurrentUser(HttpSession session) {
        ReaderInfo reader = (ReaderInfo) session.getAttribute("reader");
        if (reader == null) {
            return ApiResponse.fail(401, "未登录");
        }
        try {
            // 从数据库获取最新数据，避免缓存导致数据不同步
            ReaderInfo fresh = readerInfoService.getById(reader.getId());
            session.setAttribute("reader", fresh);
            return ApiResponse.ok(fresh);
        } catch (BusinessException e) {
            session.invalidate();
            return ApiResponse.fail(401, e.getMessage());
        }
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
     * 分页查询所有读者
     */
    @GetMapping("/list")
    public ApiResponse<List<ReaderInfo>> list(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<ReaderInfo> result = readerInfoService.queryAll(page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 搜索读者
     */
    @GetMapping("/search")
    public ApiResponse<List<ReaderInfo>> search(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        try {
            Page<ReaderInfo> result = readerInfoService.searchReaders(keyword, page, limit);
            return ApiResponse.ok((long) result.getTotalElements(), result.getContent());
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 获取读者信息
     */
    @GetMapping("/{id}")
    public ApiResponse<ReaderInfo> getById(@PathVariable Long id) {
        try {
            ReaderInfo reader = readerInfoService.getById(id);
            return ApiResponse.ok(reader);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改读者信息
     */
    @PostMapping("/update")
    public ApiResponse<ReaderInfo> update(@RequestBody ReaderInfo readerInfo, HttpSession session) {
        try {
            ReaderInfo updated = readerInfoService.update(readerInfo);
            ReaderInfo sessionReader = (ReaderInfo) session.getAttribute("reader");
            if (sessionReader != null && updated.getId() != null && updated.getId().equals(sessionReader.getId())) {
                session.setAttribute("reader", updated);
            }
            return ApiResponse.ok(updated);
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 删除读者
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            readerInfoService.delete(id);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    public ApiResponse<Void> updatePassword(@RequestParam Long id,
                                            @RequestParam String oldPassword,
                                            @RequestParam String newPassword) {
        try {
            readerInfoService.updatePassword(id, oldPassword, newPassword);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 支付罚款
     */
    @PostMapping("/payFine")
    public ApiResponse<Void> payFine(@RequestParam Long readerId, @RequestParam Double amount) {
        try {
            readerInfoService.payFine(readerId, amount);
            return ApiResponse.ok();
        } catch (BusinessException e) {
            return ApiResponse.fail(e.getCode(), e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/uploadAvatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.fail(400, "文件不能为空");
        }

        try {
            // 1. 确定上传目录 (项目根目录下的 uploads/avatars)
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. 生成新文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + suffix;

            // 3. 保存文件
            File dest = new File(dir, newFilename);
            file.transferTo(dest);

            // 4. 返回可访问的URL路径
            String avatarPath = "/uploads/avatars/" + newFilename;
            return ApiResponse.ok(avatarPath);
        } catch (IOException e) {
            log.error("上传头像失败", e);
            return ApiResponse.fail(500, "上传失败: " + e.getMessage());
        }
    }
}
