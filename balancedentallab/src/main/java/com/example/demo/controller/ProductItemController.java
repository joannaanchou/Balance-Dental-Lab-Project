package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.ProductItem;
import com.example.demo.service.ProductItemService; // 假設 service 不在 controller package

@RestController
@RequestMapping("/api")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class ProductItemController {

    @Autowired
    private ProductItemService itemService;

    @GetMapping("/items")
    public ResponseEntity<List<ProductItem>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }


    @GetMapping("/items/{id}")
    public ResponseEntity<ProductItem> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping("/items")
    public ResponseEntity<ProductItem> createItem(@RequestBody ProductItem request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createItem(request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ProductItem> updateItem(@PathVariable Long id, @RequestBody ProductItem request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
