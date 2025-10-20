package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.CaseEntity;
import com.example.demo.model.OrderDetail;
import com.example.demo.model.OrderEntity;
import com.example.demo.model.ProductDetail;
import com.example.demo.repository.CaseRepository;
import com.example.demo.repository.OrderDetailRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductDetailRepository;

@Service
@Transactional
public class OrderService {

    @Autowired 
    OrderRepository orderRepo;
    
    @Autowired 
    OrderDetailRepository detailRepo;
    
    @Autowired 
    CaseRepository caseRepo;
    
    @Autowired 
    ProductDetailRepository productRepo;

    // 呼叫 CaseService 重算案件總額
    @Autowired 
    CaseService caseService;

    /** 建立單一齒位訂單（total_amount 初始 0） */
    @Transactional
    public OrderEntity createOrder(String orderNo, String caseNo, String toothCode) {
        CaseEntity cs = caseRepo.findByCaseNo(caseNo)
                .orElseThrow(() -> new NoSuchElementException("case not found: " + caseNo));

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setCaseEntity(cs);
        order.setToothCode(toothCode);
        order.setTotalAmount(BigDecimal.ZERO);
        order = orderRepo.save(order);

        // 保持一致：建立後也回填案件總額（即使目前為 0）
        caseService.recalcCaseTotal(caseNo);
        return order;
    }

    /** 在訂單下新增一筆明細（unit_price 為 product.price 的快照；line_amount 由 DB GENERATED） */
    @Transactional
    public OrderDetail addDetail(String orderNo, String productNo, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

        OrderEntity order = orderRepo.findByOrderNo(orderNo)
                .orElseThrow(() -> new NoSuchElementException("order not found: " + orderNo));

        ProductDetail product = productRepo.findByProductNo(productNo)
                .orElseThrow(() -> new NoSuchElementException("product not found: " + productNo));

        OrderDetail od = new OrderDetail();
        od.setOrder(order);
        od.setProduct(product);
        od.setQty(qty);
        od.setUnitPrice(product.getPrice()); // 單價快照
        od = detailRepo.save(od);

        // 自動重算訂單與案件總額
        recalcAndPersistOrderTotal(order.getOrderNo());
        caseService.recalcCaseTotal(order.getCaseEntity().getCaseNo());
        return od;
    }

    /** 修改明細數量（line_amount 由 DB 依 qty*unit_price 更新） */
    @Transactional
    public OrderDetail updateDetailQty(long detailId, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

        OrderDetail od = detailRepo.findById(detailId)
                .orElseThrow(() -> new NoSuchElementException("order_detail not found: " + detailId));

        od.setQty(qty);
        OrderDetail saved = detailRepo.save(od);

        recalcAndPersistOrderTotal(saved.getOrder().getOrderNo());
        caseService.recalcCaseTotal(saved.getOrder().getCaseEntity().getCaseNo());
        return saved;
    }

    /** 更換某筆明細的商品（做法：刪舊＋以舊 qty 新增新商品） */
    @Transactional
    public OrderDetail replaceDetailProduct(long detailId, String newProductNo) {
        OrderDetail old = detailRepo.findById(detailId)
                .orElseThrow(() -> new NoSuchElementException("order_detail not found: " + detailId));

        String orderNo = old.getOrder().getOrderNo();
        int oldQty = old.getQty();

        // 刪舊
        deleteDetail(detailId);

        // 以舊 qty 新增新商品（unit_price 取新商品快照）
        return addDetail(orderNo, newProductNo, oldQty);
    }

    /** 刪除某筆明細 */
    @Transactional
    public boolean deleteDetail(long detailId) {
        OrderDetail od = detailRepo.findById(detailId).orElse(null);
        if (od == null) return false;

        String orderNo = od.getOrder().getOrderNo();
        String caseNo  = od.getOrder().getCaseEntity().getCaseNo();

        detailRepo.delete(od);

        recalcAndPersistOrderTotal(orderNo);
        caseService.recalcCaseTotal(caseNo);
        return true;
    }

    /** 刪除整張訂單（先刪所有明細，再刪主檔） */
    @Transactional
    public boolean deleteOrder(String orderNo) {
        OrderEntity order = orderRepo.findByOrderNo(orderNo).orElse(null);
        if (order == null) return false;

        String caseNo = order.getCaseEntity().getCaseNo();

        
        List<OrderDetail> details = detailRepo.findAll().stream()
                .filter(d -> d.getOrder().getOrderNo().equals(orderNo))
                .toList();
        detailRepo.deleteAll(details);

        orderRepo.delete(order);

        caseService.recalcCaseTotal(caseNo);
        return true;
    }

    @Transactional
    public OrderEntity findByOrderNo(String orderNo) {
        return orderRepo.findByOrderNo(orderNo).orElse(null);
    }

    @Transactional
    public List<OrderEntity> listByCaseNo(String caseNo) {
        return orderRepo.findByCaseEntity_CaseNo(caseNo);
    }

    /** 重新計算訂單總額並寫回（用 DB 的 GENERATED line_amount 加總） */
    private void recalcAndPersistOrderTotal(String orderNo) {
        OrderEntity order = orderRepo.findByOrderNo(orderNo)
                .orElseThrow(() -> new NoSuchElementException("order not found: " + orderNo));

        BigDecimal sum = detailRepo.sumLineAmountByOrderNo(orderNo);
        order.setTotalAmount(sum != null ? sum : BigDecimal.ZERO);
        orderRepo.save(order);
    }
}
