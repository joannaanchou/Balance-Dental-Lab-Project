package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.example.demo.model.ProductDetail;
import com.example.demo.service.ProductDetailService;


@RestController
@RequestMapping("/api")  // 複數拼寫修正
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class ProductDetailController {

    @Autowired
    private ProductDetailService productDetail;

    @GetMapping("/details")
    public ResponseEntity<List<ProductDetail>> getAllCategories() {
        return ResponseEntity.ok(productDetail.getAllDetails());
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<ProductDetail> getDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(productDetail.getDetailById(id));
    }
    
    @GetMapping("/category/{categoryNo}/items")
    public ResponseEntity<List<Map<String, String>>> getItemsByCategory(@PathVariable String categoryNo) {
        return ResponseEntity.ok(productDetail.getItemNamesByCategoryNo(categoryNo));
    }
    
    //新增
    @GetMapping("/categories/{categoryId}/items")
    public ResponseEntity<List<Map<String, Object>>> getItemsByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productDetail.getItemsByCategoryId(categoryId));
    }

    @PostMapping("/details")
    public ResponseEntity<ProductDetail> createDetail(@RequestBody ProductDetail request) {
    	ProductDetail created = productDetail.createDetail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/details/{id}")
    public ResponseEntity<ProductDetail> updateDetail(@PathVariable Long id, @RequestBody ProductDetail request) {
    	ProductDetail updated = productDetail.updateDetail(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/details/{id}")
    public ResponseEntity<Void> deleteDetail(@PathVariable Long id) {
    	productDetail.deleteDetail(id);
        return ResponseEntity.noContent().build();
    }
}

