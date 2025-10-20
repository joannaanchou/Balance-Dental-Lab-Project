package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.ProductItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
    
	@Query("SELECT MAX(p.itemNo) FROM ProductItem p")
	String findMaxItemNo();

    
    Optional<ProductItem> findByItemNo(String itemNo);
    
    boolean existsByItemNo(String itemNo);
    
  
    // 新增：透過 product_detail(category_no, item_no) 關聯，再用 product_category.id 過濾
    @Query(value = """
        SELECT i.*
        FROM product_item i
        JOIN product_detail d ON d.item_no = i.item_no
        JOIN product_category c ON c.category_no = d.category_no
        WHERE c.id = :categoryId
        GROUP BY i.id, i.item_no, i.name
        """, nativeQuery = true)
    List<ProductItem> findItemsByCategoryId(@Param("categoryId") Long categoryId);
}