package com.example.demo.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

	@Query("SELECT MAX(p.categoryNo) FROM ProductCategory p")
	String findMaxCategoryNo();

	Optional<ProductCategory> findByCategoryNo(String categoryNo);

    boolean existsByCategoryNo(String categoryNo);
}