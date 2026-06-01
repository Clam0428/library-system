package com.yx.service.impl;

import com.yx.entity.Admin;
import com.yx.exception.BusinessException;
import com.yx.repository.AdminRepository;
import com.yx.service.AdminService;
import com.yx.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 管理员服务实现
 */
@Slf4j
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    /**
     * 登录验证
     */
    @Override
    public Admin login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BusinessException("用户名或密码不能为空");
        }

        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (!adminOpt.isPresent()) {
            throw new BusinessException("用户不存在");
        }

        Admin admin = adminOpt.get();
        String encodedPassword = CommonUtils.encodPassword(password);
        if (!admin.getPassword().equals(encodedPassword)) {
            throw new BusinessException("密码错误");
        }

        log.info("管理员登录成功: username={}", username);
        return admin;
    }

    /**
     * 根据ID查询管理员
     */
    @Override
    public Optional<Admin> getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("管理员ID不能为空");
        }
        return adminRepository.findById(id);
    }

    /**
     * 根据用户名查询
     */
    @Override
    public Optional<Admin> getByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        return adminRepository.findByUsername(username);
    }

    /**
     * 添加管理员
     */
    @Override
    public Admin add(Admin admin) {
        if (admin == null || admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            throw new BusinessException("管理员信息不完整");
        }

        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 密码加密
        String encodedPassword = CommonUtils.encodPassword(admin.getPassword());
        admin.setPassword(encodedPassword);

        log.info("添加新管理员: username={}", admin.getUsername());
        return adminRepository.save(admin);
    }

    /**
     * 更新密码
     */
    @Override
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        if (id == null || oldPassword == null || newPassword == null) {
            throw new BusinessException("参数不能为空");
        }

        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (!adminOpt.isPresent()) {
            throw new BusinessException("管理员不存在");
        }

        Admin admin = adminOpt.get();
        String encodedOldPassword = CommonUtils.encodPassword(oldPassword);
        if (!admin.getPassword().equals(encodedOldPassword)) {
            throw new BusinessException("原密码错误");
        }

        String encodedNewPassword = CommonUtils.encodPassword(newPassword);
        admin.setPassword(encodedNewPassword);
        adminRepository.saveAndFlush(admin);

        log.info("管理员更新密码成功: id={}", id);
    }

    @Override
    public Admin updateProfile(Admin admin) {
        if (admin == null || admin.getId() == null) {
            throw new BusinessException("参数不能为空");
        }

        Admin current = adminRepository.findById(admin.getId())
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        String username = admin.getUsername() == null ? null : admin.getUsername().trim();
        if (username != null && !username.isEmpty() && !username.equals(current.getUsername())) {
            if (adminRepository.existsByUsernameAndIdNot(username, current.getId())) {
                throw new BusinessException("用户名已存在");
            }
            current.setUsername(username);
        }

        if (admin.getJobNumber() != null) {
            current.setJobNumber(admin.getJobNumber().trim());
        }
        if (admin.getRealName() != null) {
            current.setRealName(admin.getRealName().trim());
        }
        if (admin.getSex() != null) {
            if (admin.getSex() != 1 && admin.getSex() != 2) {
                throw new BusinessException("性别参数不合法");
            }
            current.setSex(admin.getSex());
        }
        if (admin.getTel() != null) {
            current.setTel(admin.getTel().trim());
        }
        if (admin.getAvatar() != null) {
            current.setAvatar(admin.getAvatar().trim());
        }

        Admin saved = adminRepository.saveAndFlush(current);
        log.info("管理员更新资料成功: id={}", saved.getId());
        return saved;
    }

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return adminRepository.existsByUsername(username);
    }
}
