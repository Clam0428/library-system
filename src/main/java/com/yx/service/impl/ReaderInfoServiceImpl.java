package com.yx.service.impl;

import com.yx.cache.CacheManager;
import com.yx.entity.LendList;
import com.yx.entity.ReaderInfo;
import com.yx.exception.BusinessException;
import com.yx.repository.LendListRepository;
import com.yx.repository.ReaderInfoRepository;
import com.yx.service.ReaderInfoService;
import com.yx.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 读者信息服务实现
 */
@Slf4j
@Service
@Transactional
public class ReaderInfoServiceImpl implements ReaderInfoService {

    @Autowired
    private ReaderInfoRepository readerInfoRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private LendListRepository lendListRepository;

    private static final String READER_CACHE_PREFIX = "reader:";
    private static final long CACHE_TIMEOUT = 3600;

    /**
     * 读者登录
     */
    @Override
    public ReaderInfo login(String readerNumber, String password) {
        if (readerNumber == null || password == null) {
            throw new BusinessException("学号或密码不能为空");
        }

        Optional<ReaderInfo> readerOpt = readerInfoRepository.findByReaderNumber(readerNumber);
        if (!readerOpt.isPresent()) {
            throw new BusinessException("读者不存在");
        }

        ReaderInfo reader = readerOpt.get();
        if (reader.getStatus() != null && reader.getStatus() == 1) {
            throw new BusinessException("该读者已被禁用");
        }

        String encodedPassword = CommonUtils.encodPassword(password);
        if (!reader.getPassword().equals(encodedPassword)) {
            throw new BusinessException("密码错误");
        }

        log.info("读者登录成功: readerNumber={}", readerNumber);
        return reader;
    }

    /**
     * 分页查询所有读者
     */
    @Override
    public Page<ReaderInfo> queryAll(Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        return readerInfoRepository.findAll(PageRequest.of(page - 1, limit));
    }

    /**
     * 搜索读者
     */
    @Override
    public Page<ReaderInfo> searchReaders(String keyword, Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        return readerInfoRepository.searchReaders(keyword, PageRequest.of(page - 1, limit));
    }

    /**
     * 根据ID查询
     */
    @Override
    public ReaderInfo getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("读者ID不能为空");
        }

        String cacheKey = READER_CACHE_PREFIX + id;
        Object cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return (ReaderInfo) cached;
        }

        Optional<ReaderInfo> readerOpt = readerInfoRepository.findById(id);
        if (!readerOpt.isPresent()) {
            throw new BusinessException("读者不存在");
        }

        ReaderInfo reader = readerOpt.get();
        // 克隆对象以避免缓存和数据库对象引用相同
        ReaderInfo cachedReader = cloneReaderInfo(reader);
        cacheManager.set(cacheKey, cachedReader, CACHE_TIMEOUT);
        return reader;
    }
    
    /**
     * 克隆读者信息对象
     */
    private ReaderInfo cloneReaderInfo(ReaderInfo source) {
        if (source == null) return null;
        ReaderInfo clone = new ReaderInfo();
        clone.setId(source.getId());
        clone.setUsername(source.getUsername());
        clone.setReaderNumber(source.getReaderNumber());
        clone.setRealName(source.getRealName());
        clone.setSex(source.getSex());
        clone.setTel(source.getTel());
        clone.setEmail(source.getEmail());
        clone.setDepartment(source.getDepartment());
        clone.setAddress(source.getAddress());
        clone.setBirthday(source.getBirthday());
        clone.setStatus(source.getStatus());
        clone.setBorrowCount(source.getBorrowCount());
        clone.setFineAmount(source.getFineAmount());
        clone.setAvatar(source.getAvatar());
        clone.setRegisterDate(source.getRegisterDate());
        clone.setCreateTime(source.getCreateTime());
        clone.setUpdateTime(source.getUpdateTime());
        return clone;
    }

    /**
     * 根据读者号查询
     */
    @Override
    public ReaderInfo getByReaderNumber(String readerNumber) {
        if (readerNumber == null || readerNumber.trim().isEmpty()) {
            throw new BusinessException("读者号不能为空");
        }

        Optional<ReaderInfo> readerOpt = readerInfoRepository.findByReaderNumber(readerNumber);
        if (!readerOpt.isPresent()) {
            throw new BusinessException("读者不存在");
        }
        return readerOpt.get();
    }

    /**
     * 添加读者
     */
    @Override
    public ReaderInfo add(ReaderInfo readerInfo) {
        if (readerInfo == null) {
            throw new BusinessException("读者信息不能为空");
        }

        if (readerInfo.getReaderNumber() == null || readerInfo.getReaderNumber().trim().isEmpty()) {
            throw new BusinessException("学号不能为空");
        }

        if (readerInfoRepository.findByReaderNumber(readerInfo.getReaderNumber()).isPresent()) {
            throw new BusinessException("该学号已被注册");
        }

        if (readerInfo.getPassword() == null || readerInfo.getPassword().trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        // 密码加密
        String encodedPassword = CommonUtils.encodPassword(readerInfo.getPassword());
        readerInfo.setPassword(encodedPassword);

        if (readerInfo.getUsername() == null || readerInfo.getUsername().trim().isEmpty()) {
            readerInfo.setUsername(readerInfo.getReaderNumber());
        }

        log.info("添加新读者: readerNumber={}", readerInfo.getReaderNumber());
        return readerInfoRepository.save(readerInfo);
    }

    /**
     * 更新读者信息
     */
    @Override
    public ReaderInfo update(ReaderInfo readerInfo) {
        if (readerInfo == null || readerInfo.getId() == null) {
            throw new BusinessException("读者ID不能为空");
        }

        ReaderInfo existing = readerInfoRepository.findById(readerInfo.getId())
                .orElseThrow(() -> new BusinessException("读者不存在"));

        log.info("更新读者信息: id={}", readerInfo.getId());
        
        // 保持一些不应通过此接口修改的字段，或者手动复制字段以防丢失
        if (readerInfo.getRealName() != null) existing.setRealName(readerInfo.getRealName());
        if (readerInfo.getSex() != null) {
            if (readerInfo.getSex() != 1 && readerInfo.getSex() != 2) {
                throw new BusinessException("性别参数不合法");
            }
            existing.setSex(readerInfo.getSex());
        }
        if (readerInfo.getTel() != null) existing.setTel(readerInfo.getTel());
        if (readerInfo.getEmail() != null) existing.setEmail(readerInfo.getEmail());
        if (readerInfo.getDepartment() != null) existing.setDepartment(readerInfo.getDepartment());
        if (readerInfo.getStatus() != null) existing.setStatus(readerInfo.getStatus());
        if (readerInfo.getAddress() != null) existing.setAddress(readerInfo.getAddress());
        if (readerInfo.getBirthday() != null) existing.setBirthday(readerInfo.getBirthday());
        if (readerInfo.getBorrowCount() != null) existing.setBorrowCount(readerInfo.getBorrowCount());
        if (readerInfo.getFineAmount() != null) existing.setFineAmount(readerInfo.getFineAmount());
        if (readerInfo.getAvatar() != null) existing.setAvatar(readerInfo.getAvatar());
        
        // 如果提供了新密码，且与旧密码不同，则加密后更新
        if (readerInfo.getPassword() != null && !readerInfo.getPassword().trim().isEmpty()) {
            if (!readerInfo.getPassword().equals(existing.getPassword())) {
                existing.setPassword(CommonUtils.encodPassword(readerInfo.getPassword()));
            }
        }

        ReaderInfo saved = readerInfoRepository.save(existing);
        cacheManager.delete(READER_CACHE_PREFIX + readerInfo.getId());
        return saved;
    }

    /**
     * 删除读者
     */
    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("读者ID不能为空");
        }

        if (!readerInfoRepository.existsById(id)) {
            throw new BusinessException("读者不存在");
        }

        log.info("删除读者: id={}", id);
        readerInfoRepository.deleteById(id);
        cacheManager.delete(READER_CACHE_PREFIX + id);
    }

    /**
     * 检查学号是否存在
     */
    @Override
    public boolean readerNumberExists(String readerNumber) {
        if (readerNumber == null || readerNumber.trim().isEmpty()) {
            return false;
        }
        return readerInfoRepository.findByReaderNumber(readerNumber).isPresent();
    }

    /**
     * 更新读者密码
     */
    @Override
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        if (id == null || oldPassword == null || newPassword == null) {
            throw new BusinessException("参数不能为空");
        }

        ReaderInfo reader = getById(id);
        String encodedOldPassword = CommonUtils.encodPassword(oldPassword);
        if (!reader.getPassword().equals(encodedOldPassword)) {
            throw new BusinessException("原密码错误");
        }

        String encodedNewPassword = CommonUtils.encodPassword(newPassword);
        reader.setPassword(encodedNewPassword);
        readerInfoRepository.saveAndFlush(reader);
        cacheManager.delete(READER_CACHE_PREFIX + id);

        log.info("读者更新密码成功: id={}", id);
    }

    /**
     * 增加罚款金额
     */
    @Override
    public void addFine(Long readerId, Double fineAmount) {
        ReaderInfo reader = getById(readerId);
        Double currentFine = reader.getFineAmount() != null ? reader.getFineAmount() : 0.0;
        reader.setFineAmount(currentFine + fineAmount);
        readerInfoRepository.save(reader);
        cacheManager.delete(READER_CACHE_PREFIX + readerId);
        log.info("增加读者罚款: readerId={}, fineAmount={}", readerId, fineAmount);
    }

    /**
     * 支付罚款
     */
    @Override
    public void payFine(Long readerId, Double amount) {
        if (readerId == null) {
            throw new BusinessException("参数不能为空");
        }

        ReaderInfo reader = getById(readerId);
        Double currentFine = reader.getFineAmount() != null ? reader.getFineAmount() : 0.0;
        if (currentFine <= 0) {
            throw new BusinessException("暂无待缴罚款");
        }

        lendListRepository.clearFineByReaderId(readerId);
        readerInfoRepository.clearFineAmount(readerId);
        cacheManager.delete(READER_CACHE_PREFIX + readerId);
        log.info("读者支付罚款: readerId={}, amount={}", readerId, amount);
    }
}
