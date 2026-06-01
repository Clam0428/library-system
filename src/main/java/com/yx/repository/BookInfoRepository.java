package com.yx.repository;

import com.yx.entity.BookInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Lock;
import javax.persistence.LockModeType;
import java.util.Optional;

/**
 * 图书数据访问层
 */
@Repository
public interface BookInfoRepository extends JpaRepository<BookInfo, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookInfo b where b.id = :id")
    Optional<BookInfo> findByIdWithLock(@Param("id") Long id);
    /**
     * 根据位置查询
     */
    BookInfo findByLocation(String location);

    /**
     * 根据类型ID分页查询
     */
    Page<BookInfo> findByTypeId(Long typeId, Pageable pageable);

    /**
     * 检查位置是否存在
     */
    boolean existsByLocation(String location);

    /**
     * 根据状态分页查询
     */
    Page<BookInfo> findByStatus(Integer status, Pageable pageable);

    /**
     * 模糊搜索书籍
     */
    @Query("SELECT b FROM BookInfo b WHERE b.name LIKE %:keyword% OR b.author LIKE %:keyword%")
    Page<BookInfo> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计所有图书的总库存
     */
    @Query("SELECT COALESCE(SUM(b.stock), 0) FROM BookInfo b")
    long sumStock();
}
