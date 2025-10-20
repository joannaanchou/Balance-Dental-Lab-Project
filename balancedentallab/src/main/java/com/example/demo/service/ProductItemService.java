package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ProductCategory;
import com.example.demo.model.ProductItem;
import com.example.demo.repository.ProductCategoryRepository;
import com.example.demo.repository.ProductItemRepository;

@Service
@Transactional
public class ProductItemService {


    

    @Autowired
    private ProductItemRepository itemRepo;

 
    

    public List<ProductItem> getAllItems() {
        return itemRepo.findAll();
    }


    public ProductItem getItemById(Long id) {
        return itemRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("項目不存在: " + id));
    }

    private String generateItemNo() {
        String maxItemNo = itemRepo.findMaxItemNo();
        if (maxItemNo == null) {
            return "I001";
        }

        int num = Integer.parseInt(maxItemNo.substring(1)); // 去掉開頭的 'I'
        return String.format("I%03d", num + 1);
    }
    public ProductItem createItem(ProductItem request) {
        ProductItem item = new ProductItem();
        item.setItemNo(generateItemNo()); // 自動編號
        item.setName(request.getName());
        
        return itemRepo.save(item);
    }


    public ProductItem updateItem(Long id, ProductItem request) {
        ProductItem Item = itemRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("項目不存在"+id));

        if (!Item.getItemNo().equals(request.getItemNo()) &&
        		itemRepo.existsByItemNo(request.getItemNo())) {
                throw new RuntimeException("類別編號已存在: " + request.getItemNo());
            }

        Item.setItemNo(request.getItemNo());
        Item.setName(request.getName());

            return itemRepo.save(Item);
    }
    
    
    //新增
    public List<ProductItem> findByCategoryId(Long categoryId) {
        return itemRepo.findItemsByCategoryId(categoryId);
    }

    public void deleteItem(Long id) {
        if (!itemRepo.existsById(id)) {
            throw new RuntimeException("項目不存在: " + id);
        }
        itemRepo.deleteById(id);
    }
}
