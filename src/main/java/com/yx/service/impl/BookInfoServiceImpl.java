package com.yx.service.impl;

import com.yx.cache.CacheManager;
import com.yx.entity.BookInfo;
import com.yx.exception.BusinessException;
import com.yx.repository.BookInfoRepository;
import com.yx.repository.TypeInfoRepository;
import com.yx.service.BookInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图书信息服务实现
 */
@Slf4j
@Service
@Transactional
public class BookInfoServiceImpl implements BookInfoService {

    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Autowired
    private TypeInfoRepository typeInfoRepository;

    @Autowired
    private CacheManager cacheManager;

    private static final String BOOK_CACHE_PREFIX = "book:";
    private static final String BOOK_STATISTICS_CACHE = "book:statistics";
    private static final long CACHE_TIMEOUT = 3600; // 1小时

    /**
     * 分页查询所有图书
     */
    @Override
    public Page<BookInfo> queryAll(Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        log.info("查询图书列表: page={}, limit={}", page, limit);

        // 尝试从缓存获取
        String cacheKey = "book:list:all:" + page + ":" + limit;
        Object cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return (Page<BookInfo>) cached;
        }

        Page<BookInfo> result = bookInfoRepository.findAll(PageRequest.of(page - 1, limit));
        populateTypeNames(result.getContent());

        // 存入缓存
        cacheManager.set(cacheKey, result, CACHE_TIMEOUT);
        return result;
    }

    /**
     * 按类型分页查询
     */
    @Override
    public Page<BookInfo> queryByType(Long typeId, Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        log.info("按类型查询图书: typeId={}, page={}, limit={}", typeId, page, limit);

        // 尝试从缓存获取
        String cacheKey = "book:list:type:" + typeId + ":" + page + ":" + limit;
        Object cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return (Page<BookInfo>) cached;
        }

        Page<BookInfo> result = bookInfoRepository.findByTypeId(typeId, PageRequest.of(page - 1, limit));
        populateTypeNames(result.getContent());

        // 存入缓存
        cacheManager.set(cacheKey, result, CACHE_TIMEOUT);
        return result;
    }

    /**
     * 搜索图书
     */
    @Override
    public Page<BookInfo> searchBooks(String keyword, Integer page, Integer limit) {
        page = page == null || page < 1 ? 1 : page;
        limit = limit == null || limit < 1 ? 10 : limit;
        log.info("搜索图书: keyword={}, page={}, limit={}", keyword, page, limit);
        Page<BookInfo> result = bookInfoRepository.searchBooks(keyword, PageRequest.of(page - 1, limit));
        populateTypeNames(result.getContent());
        return result;
    }

    /**
     * 查询单本图书详情
     */
    @Override
    public BookInfo getById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("图书ID不能为空");
        }

        // 先查询缓存
        String cacheKey = BOOK_CACHE_PREFIX + id;
        Object cached = cacheManager.get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取图书: id={}", id);
            return (BookInfo) cached;
        }

        // 再查询数据库
        Optional<BookInfo> bookOpt = bookInfoRepository.findById(id);
        if (!bookOpt.isPresent()) {
            throw new BusinessException("图书不存在");
        }

        BookInfo book = bookOpt.get();
        populateTypeNames(Collections.singletonList(book));
        // 放入缓存
        cacheManager.set(cacheKey, book, CACHE_TIMEOUT);
        log.debug("从数据库获取图书并缓存: id={}", id);
        return book;
    }

    private void populateTypeNames(List<BookInfo> books) {
        if (books == null || books.isEmpty()) return;
        Set<Long> typeIds = books.stream().map(BookInfo::getTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (typeIds.isEmpty()) return;
        
        Map<Long, String> typeMap = typeInfoRepository.findAllById(typeIds).stream()
                .collect(Collectors.toMap(com.yx.entity.TypeInfo::getId, com.yx.entity.TypeInfo::getName));
        
        books.forEach(b -> b.setTypeName(typeMap.get(b.getTypeId())));
    }

    /**
     * 添加图书
     */
    @Override
    public BookInfo add(BookInfo bookInfo) {
        if (bookInfo == null) {
            throw new BusinessException("图书信息不能为空");
        }
        if (bookInfo.getName() == null || bookInfo.getName().trim().isEmpty()) {
            throw new BusinessException("图书名称不能为空");
        }
        if (bookInfo.getLocation() != null && bookInfoRepository.existsByLocation(bookInfo.getLocation())) {
            throw new BusinessException("图书存放位置已存在");
        }

        log.info("添加新图书: {}", bookInfo.getName());
        
        // 自动计算状态
        calculateStatus(bookInfo);
        
        BookInfo saved = bookInfoRepository.save(bookInfo);
        // 清除统计缓存
        cacheManager.delete(BOOK_STATISTICS_CACHE);
        cacheManager.deletePattern("book:list:*");
        return saved;
    }

    /**
     * 更新图书
     */
    @Override
    public BookInfo update(BookInfo bookInfo) {
        if (bookInfo == null || bookInfo.getId() == null) {
            throw new BusinessException("图书ID不能为空");
        }

        Long id = bookInfo.getId();
        if (!bookInfoRepository.existsById(id)) {
            throw new BusinessException("图书不存在");
        }

        log.info("更新图书: id={}", id);
        
        // 自动计算状态
        calculateStatus(bookInfo);
        
        BookInfo saved = bookInfoRepository.save(bookInfo);
        // 清除缓存
        cacheManager.delete(BOOK_CACHE_PREFIX + id);
        cacheManager.deletePattern("book:list:*");
        cacheManager.delete(BOOK_STATISTICS_CACHE);
        cacheManager.deletePattern("book:list:*");
        return saved;
    }

    /**
     * 删除图书
     */
    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("图书ID不能为空");
        }

        if (!bookInfoRepository.existsById(id)) {
            throw new BusinessException("图书不存在");
        }

        log.info("删除图书: id={}", id);
        bookInfoRepository.deleteById(id);
        // 清除缓存
        cacheManager.delete(BOOK_CACHE_PREFIX + id);
        cacheManager.delete(BOOK_STATISTICS_CACHE);
        cacheManager.deletePattern("book:list:*");
    }

    /**
     * 批量删除
     */
    @Override
    public void deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("删除ID列表不能为空");
        }

        log.info("批量删除图书: count={}", ids.length);
        for (Long id : ids) {
            bookInfoRepository.deleteById(id);
            cacheManager.delete(BOOK_CACHE_PREFIX + id);
        }
        cacheManager.delete(BOOK_STATISTICS_CACHE);
        cacheManager.deletePattern("book:list:*");
    }

    /**
     * 根据位置获取图书
     */
    @Override
    public BookInfo getByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new BusinessException("存放位置不能为空");
        }
        log.debug("根据位置查询图书: location={}", location);
        return bookInfoRepository.findByLocation(location);
    }

    /**
     * 自动计算图书状态
     * 0=正常可用，1=已借出，2=禁用
     */
    private void calculateStatus(BookInfo book) {
        // 如果管理员手动设置为禁用(2)，则保持禁用
        if (book.getStatus() != null && book.getStatus() == 2) {
            return;
        }
        
        // 计算剩余库存
        int stock = book.getStock() != null ? book.getStock() : 0;
        int borrowCount = book.getBorrowCount() != null ? book.getBorrowCount() : 0;
        int available = stock - borrowCount;
        
        if (available <= 0) {
            book.setStatus(1); // 设为已借出
        } else {
            book.setStatus(0); // 设为正常可用
        }
    }

    /**
     * 更新图书库存
     */
    @Override
    public void updateStock(Long id, Integer stock) {
        if (id == null || stock == null) {
            throw new BusinessException("参数不能为空");
        }

        BookInfo book = getById(id);
        book.setStock(stock);
        bookInfoRepository.save(book);
        cacheManager.delete(BOOK_CACHE_PREFIX + id);
        cacheManager.deletePattern("book:list:*");
        log.info("更新图书库存: id={}, stock={}", id, stock);
    }

    /**
     * 检查图书是否可以借阅
     */
    @Override
    public boolean canBorrow(Long id, Integer count) {
        BookInfo book = getById(id);
        int available = book.getStock() - (book.getBorrowCount() != null ? book.getBorrowCount() : 0);
        return available >= count;
    }

    /**
     * 借书时更新图书状态
     */
    @Override
    public void updateOnBorrow(Long id, Integer count) {
        BookInfo book = getById(id);
        if (!canBorrow(id, count)) {
            throw new BusinessException("图书库存不足");
        }
        book.setBorrowCount((book.getBorrowCount() != null ? book.getBorrowCount() : 0) + count);
        // 如果库存全部借出，且状态不是“禁用”，则更新为“已借出”
        if (book.getBorrowCount().equals(book.getStock()) && (book.getStatus() == null || book.getStatus() == 0)) {
            book.setStatus(1); // 标记为已借出
        }
        bookInfoRepository.save(book);
        cacheManager.delete(BOOK_CACHE_PREFIX + id);
        log.info("借书时更新图书: id={}, borrowCount={}", id, book.getBorrowCount());
    }

    /**
     * 还书时更新图书状态
     */
    @Override
    public void updateOnReturn(Long id) {
        BookInfo book = getById(id);
        int current = book.getBorrowCount() != null ? book.getBorrowCount() : 0;
        if (current > 0) {
            book.setBorrowCount(current - 1);
        }
        // 如果状态是“已借出”，现在有余量了，恢复为“正常可用”
        if (book.getStatus() != null && book.getStatus() == 1) {
            book.setStatus(0); 
        }
        bookInfoRepository.save(book);
        cacheManager.delete(BOOK_CACHE_PREFIX + id);
        log.info("还书时更新图书: id={}, borrowCount={}", id, book.getBorrowCount());
    }

    /**
     * 获取图书分类统计
     */
    @Override
    public Object getTypeStatistics() {
        // 尝试从缓存获取
        Object cached = cacheManager.get(BOOK_STATISTICS_CACHE);
        if (cached != null) {
            log.debug("从缓存获取图书统计");
            return cached;
        }

        log.debug("查询图书分类统计");
        List<BookInfo> allBooks = bookInfoRepository.findAll();
        Map<Long, Integer> statistics = new HashMap<>();

        for (BookInfo book : allBooks) {
            Long typeId = book.getTypeId();
            statistics.put(typeId, statistics.getOrDefault(typeId, 0) + 1);
        }

        // 将统计转换为包含类型名称的列表结构，便于前端直接显示
        List<Map<String, Object>> result = new ArrayList<>();
        if (!statistics.isEmpty()) {
            // 批量查询类型名称
            List<Long> typeIds = new ArrayList<>(statistics.keySet());
            Map<Long, String> typeNameMap = typeInfoRepository.findAllById(typeIds).stream()
                    .collect(Collectors.toMap(com.yx.entity.TypeInfo::getId, com.yx.entity.TypeInfo::getName));
            for (Map.Entry<Long, Integer> entry : statistics.entrySet()) {
                Long typeId = entry.getKey();
                Integer count = entry.getValue();
                String name = typeNameMap.getOrDefault(typeId, "未知(" + typeId + ")");
                Map<String, Object> item = new HashMap<>();
                item.put("name", name);
                item.put("count", count);
                result.add(item);
            }
        }

        // 放入缓存
        cacheManager.set(BOOK_STATISTICS_CACHE, result, CACHE_TIMEOUT);
        return result;
    }
}
