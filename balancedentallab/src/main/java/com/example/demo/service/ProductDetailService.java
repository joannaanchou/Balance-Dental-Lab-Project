package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.ProductDetail;
import com.example.demo.repository.ProductDetailRepository;

@Service
@Transactional
public class ProductDetailService {

    @Autowired
    private ProductDetailRepository repository;

    // 取得所有商品明細
    public List<ProductDetail> getAllDetails() {
        return repository.findAll();
    }
    
    // 透過 ID 查詢單筆商品明細
    public ProductDetail getDetailById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品明細不存在: " + id));
    }
    
    public List<Map<String, String>> getItemNamesByCategoryNo(String categoryNo) {
        return repository.findAll().stream()
                .filter(d -> d.getCategory().getCategoryNo().equals(categoryNo))
                .map(d -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", d.getId().toString());
                    map.put("name", d.getItem().getName());
                    return map;
                })
                .distinct()
                .collect(Collectors.toList());
    }
    
    
    //新增
    public List<Map<String, Object>> getItemsByCategoryId(Long categoryId) {
        return repository.findByCategoryId(categoryId).stream()
            .map(pd -> {
                Map<String, Object> m = new HashMap<>();
                // 用「品項」的 id 與 name，並帶出對應的 productNo（存 order_detail 用）
                m.put("id",       pd.getItem().getId());
                m.put("name",     pd.getItem().getName());
                m.put("productNo",pd.getProductNo());
                return m;
            })
           
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(m -> (String)m.get("name"), m -> m, (a,b) -> a),
                m -> m.values().stream().toList()
            ));
    }
    
    // 新增商品明細
    public ProductDetail createDetail(ProductDetail request) {
        // 自動產生 ProductNo
        if (request.getProductNo() == null || request.getProductNo().isEmpty()) {
            request.setProductNo(generateProductNo());
        }

        if (repository.existsByProductNo(request.getProductNo())) {
            throw new RuntimeException("商品編號已存在: " + request.getProductNo());
        }

        ProductDetail detail = new ProductDetail();
        detail.setProductNo(request.getProductNo());
        detail.setCategory(request.getCategory());
        detail.setItem(request.getItem());
        detail.setPrice(request.getPrice());

        return repository.save(detail);
    }

    // 自動產生商品編號 P001、P002、...
    private String generateProductNo() {
        String maxNo = repository.findMaxProductNo(); // 例如回傳 P008
        if (maxNo == null) {
            return "P001";
        }

        int num = Integer.parseInt(maxNo.substring(1)); // 取得數字部分
        num++;
        return String.format("P%03d", num); // 補 0 至三位數
    }


    // 更新商品明細
    public ProductDetail updateDetail(Long id, ProductDetail request) {
        ProductDetail detail = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品明細不存在: " + id));

        // 檢查商品編號是否變更且重複
        if (!detail.getProductNo().equals(request.getProductNo()) &&
            repository.existsByProductNo(request.getProductNo())) {
            throw new RuntimeException("商品編號已存在: " + request.getProductNo());
        }

        detail.setProductNo(request.getProductNo());
        detail.setCategory(request.getCategory());
        detail.setItem(request.getItem());
        detail.setPrice(request.getPrice());

        return repository.save(detail);
    }

    // 刪除商品明細
    public void deleteDetail(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("商品明細不存在: " + id);
        }
        repository.deleteById(id);
    }
}
