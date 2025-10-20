package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{

	// 依orderNo查單張訂單
    Optional<OrderEntity> findByOrderNo(String orderNo);

    // 查某case底下所有order
    List<OrderEntity> findByCaseEntity_CaseNo(String caseNo);

    // 依orderNo 進行刪除（Service 先刪明細再刪訂單）
    void deleteByOrderNo(String orderNo);
    
    
    @Query("select coalesce(sum(o.totalAmount), 0) " +
            "from OrderEntity o " +
            "where o.caseEntity.caseNo = :caseNo")
     BigDecimal sumTotalsByCaseNo(String caseNo);
}
