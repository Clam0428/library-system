package com.yx.service;

import com.yx.entity.BookInfo;
import org.springframework.data.domain.Page;

/**
 * 图书信息服务接口
 */
public interface BookInfoService {
    /**
     * 分页查询所有图书
     */
    Page<BookInfo> queryAll(Integer page, Integer limit);

    /**
     * 按类型分页查询
     */
    Page<BookInfo> queryByType(Long typeId, Integer page, Integer limit);

    /**
     * 搜索图书
     */
    Page<BookInfo> searchBooks(String keyword, Integer page, Integer limit);

    /**
     * 查询单本图书详情
     */
    BookInfo getById(Long id);

    /**
     * 添加图书
     */
    BookInfo add(BookInfo bookInfo);

    /**
     * 更新图书
     */
    BookInfo update(BookInfo bookInfo);

    /**
     * 删除图书
     */
    void delete(Long id);

    /**
     * 批量删除
     */
    void deleteBatch(Long[] ids);

    /**
     * 根据位置获取图书
     */
    BookInfo getByLocation(String location);

    /**
     * 更新图书库存
     */
    void updateStock(Long id, Integer stock);

    /**
     * 检查图书是否可以借阅
     */
    boolean canBorrow(Long id, Integer count);

    /**
     * 借书时更新图书状态
     */
    void updateOnBorrow(Long id, Integer count);

    /**
     * 还书时更新图书状态
     */
    void updateOnReturn(Long id);

    /**
     * 获取图书分类统计
     */
    Object getTypeStatistics();
}
