package com.yx.service;

import com.yx.entity.ReaderInfo;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * 读者信息服务接口
 */
public interface ReaderInfoService {
    /**
     * 读者登录
     */
    ReaderInfo login(String readerNumber, String password);

    /**
     * 分页查询所有读者
     */
    Page<ReaderInfo> queryAll(Integer page, Integer limit);

    /**
     * 搜索读者
     */
    Page<ReaderInfo> searchReaders(String keyword, Integer page, Integer limit);

    /**
     * 根据ID查询
     */
    ReaderInfo getById(Long id);

    /**
     * 根据读者号查询
     */
    ReaderInfo getByReaderNumber(String readerNumber);

    /**
     * 添加读者
     */
    ReaderInfo add(ReaderInfo readerInfo);

    /**
     * 更新读者信息
     */
    ReaderInfo update(ReaderInfo readerInfo);

    /**
     * 删除读者
     */
    void delete(Long id);

    /**
     * 检查学号是否存在
     */
    boolean readerNumberExists(String readerNumber);

    /**
     * 更新读者密码
     */
    void updatePassword(Long id, String oldPassword, String newPassword);

    /**
     * 增加罚款金额
     */
    void addFine(Long readerId, Double fineAmount);

    /**
     * 支付罚款
     */
    void payFine(Long readerId, Double amount);
}
