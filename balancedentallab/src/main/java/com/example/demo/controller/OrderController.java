package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.OrderDetail;
import com.example.demo.model.OrderEntity;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class OrderController {

    @Autowired
    OrderService srv; 
    
    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    OrderDetailRepository orderDetailRepository;
    

    /** 建立單一齒位訂單（body 需帶 orderNo、caseEntity.caseNo、toothCode） */
    @PostMapping
    public ResponseEntity<OrderEntity> createOrder(@RequestBody OrderEntity payload) {
        String orderNo   = payload.getOrderNo();
        String caseNo    = (payload.getCaseEntity() != null) ? payload.getCaseEntity().getCaseNo() : null;
        String toothCode = payload.getToothCode();
        return ResponseEntity.ok(srv.createOrder(orderNo, caseNo, toothCode));
    }

    /** 在訂單下新增一筆明細（body 帶 product.productNo、qty；orderNo 走 path） */
    @PostMapping("/{orderNo}/details")
    public ResponseEntity<OrderDetail> addDetail(
            @PathVariable String orderNo,
            @RequestBody OrderDetail payload) {
        String productNo = (payload.getProduct() != null) ? payload.getProduct().getProductNo() : null;
        int qty = payload.getQty();
        return ResponseEntity.ok(srv.addDetail(orderNo, productNo, qty));
    }

    /** 修改某筆明細的數量（body 只需帶 qty） */
    @PatchMapping("/details/{detailId}")
    public ResponseEntity<OrderDetail> updateDetailQty(
            @PathVariable long detailId,
            @RequestBody OrderDetail payload) {
        return ResponseEntity.ok(srv.updateDetailQty(detailId, payload.getQty()));
    }

    /** 更換某筆明細的商品（做法A：刪舊＋新增；body 帶 product.productNo） */
    @PatchMapping("/details/{detailId}/replace")
    public ResponseEntity<OrderDetail> replaceDetailProduct(
            @PathVariable long detailId,
            @RequestBody OrderDetail payload) {
        String newProductNo = (payload.getProduct() != null) ? payload.getProduct().getProductNo() : null;
        return ResponseEntity.ok(srv.replaceDetailProduct(detailId, newProductNo));
    }

    /** 刪除某筆明細 */
    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<?> deleteDetail(@PathVariable long detailId) {
        boolean ok = srv.deleteDetail(detailId);
        return ok ? ResponseEntity.ok("Delete detail OK") : ResponseEntity.noContent().build();
    }

    /** 刪除整張訂單 */
    @DeleteMapping("/{orderNo}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderNo) {
        boolean ok = srv.deleteOrder(orderNo);
        return ok ? ResponseEntity.ok("Delete order OK") : ResponseEntity.noContent().build();
    }

    /** 查某案件底下所有訂單（用 query 參數 caseNo） */
    @GetMapping
    public List<OrderEntity> listByCase(@RequestParam String caseNo) {
        return srv.listByCaseNo(caseNo);
    }

    /** 查單張訂單 */
//    @GetMapping("/{orderNo}")
//    public ResponseEntity<OrderEntity> getOrder(@PathVariable String orderNo) {
//        OrderEntity o = srv.findByOrderNo(orderNo);
//        return o == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(o);
//    }
    
    
    @GetMapping("/{orderNo}/details")
    public ResponseEntity<List<OrderDetailDto>> listDetails(@PathVariable String orderNo) {
        // 先確認訂單存在（也能順便幫你抓掉 orderNo 不一致的情況）
        var orderOpt = orderRepository.findByOrderNo(orderNo);
        if (orderOpt.isEmpty()) return ResponseEntity.notFound().build();

        var details = orderDetailRepository.findAllByOrderNo(orderNo);

        // 回前端最需要的欄位（含 productNo 與 item 名稱）
        var dtos = details.stream().map(d -> new OrderDetailDto(
                d.getId(),
                d.getProduct() != null ? d.getProduct().getProductNo() : null,
                d.getQty(),
                d.getProduct() != null && d.getProduct().getItem() != null ? d.getProduct().getItem().getName() : null
        )).toList();

        return ResponseEntity.ok(dtos);
    }

    public record OrderDetailDto(Long id, String productNo, Integer qty, String itemName) {}


}
