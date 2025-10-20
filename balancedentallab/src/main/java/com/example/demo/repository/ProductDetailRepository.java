package com.example.demo.repository;

import com.example.demo.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    Optional<ProductDetail> findByProductNo(String productNo);
    Optional<ProductDetail> findByCategory_IdAndItem_Id(Long categoryId, Long itemId);
    boolean existsByProductNo(String productNo);
    
    @Query("SELECT MAX(p.productNo) FROM ProductDetail p")
    String findMaxProductNo();

    
    @Query("select distinct pd.item.name from ProductDetail pd " +
            "where pd.category.categoryNo = :categoryNo " +
            "order by pd.item.name")
     List<String> findDistinctItemNamesByCategoryNo(@Param("categoryNo") String categoryNo);
    
    
    
    @Query("""
    		  select pd 
    		  from ProductDetail pd
    		  where pd.category.id = :categoryId
    		  order by pd.item.name
    		""")
    List<ProductDetail> findByCategoryId(@Param("categoryId") Long categoryId);
}
