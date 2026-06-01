package com.yx.service;

import com.yx.entity.Admin;

import java.util.Optional;

/**
 * 管理员服务接口
 */
public interface AdminService {
    /**
     * 登录验证
     */
    Admin login(String username, String password);

    /**
     * 根据ID查询管理员
     */
    Optional<Admin> getById(Long id);

    /**
     * 根据用户名查询
     */
    Optional<Admin> getByUsername(String username);

    /**
     * 添加管理员
     */
    Admin add(Admin admin);

    /**
     * 更新密码
     */
    void updatePassword(Long id, String oldPassword, String newPassword);

    Admin updateProfile(Admin admin);

    /**
     * 检查用户名是否存在
     */
    boolean usernameExists(String username);
}
