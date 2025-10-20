package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ProductCategory;
import com.example.demo.repository.ProductCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryService {
	 @Autowired
    private  ProductCategoryRepository repository;

    public List<ProductCategory> getAllCategories() {
        return repository.findAll();
    }

    public ProductCategory getCategoryById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("類別不存在: " + id));
    }

    private String generateCategoryNo() {
        String maxCategoryNo = repository.findMaxCategoryNo(); // 需要自己寫這個查詢
        if (maxCategoryNo == null) {
            return "CA001";
        }

        int num = Integer.parseInt(maxCategoryNo.substring(2)); // 跳過 "CA"
        num++;
        return String.format("CA%03d", num); // 補0格式 CA001
    }

    
    public ProductCategory createCategory(ProductCategory request) {
        if (request.getCategoryNo() == null || request.getCategoryNo().isEmpty()) {
            request.setCategoryNo(generateCategoryNo()); // 自動產生
        } else if (repository.existsByCategoryNo(request.getCategoryNo())) {
            throw new RuntimeException("類別編號已存在: " + request.getCategoryNo());
        }

        ProductCategory category = new ProductCategory();
        category.setCategoryNo(request.getCategoryNo());
        category.setName(request.getName());

        return repository.save(category);
    }


    public ProductCategory updateCategory(Long id, ProductCategory request) {
        ProductCategory category = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("類別不存在: " + id));

        if (!category.getCategoryNo().equals(request.getCategoryNo()) &&
            repository.existsByCategoryNo(request.getCategoryNo())) {
            throw new RuntimeException("類別編號已存在: " + request.getCategoryNo());
        }

        category.setCategoryNo(request.getCategoryNo());
        category.setName(request.getName());

        return repository.save(category);
    }

    public void deleteCategory(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("類別不存在: " + id);
        }
        repository.deleteById(id);
    }
}
