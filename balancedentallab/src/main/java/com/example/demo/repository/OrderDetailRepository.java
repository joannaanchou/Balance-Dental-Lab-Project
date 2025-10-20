package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

	
    // DB 已定義 GENERATED 欄位→ 可直接加總 line_amount 取得該訂單總額
    @Query(value = """
        SELECT COALESCE(SUM(od.line_amount), 0)
        FROM `order_detail` od
        WHERE od.order_no = ?1
        """, nativeQuery = true)
    
    BigDecimal sumLineAmountByOrderNo(String orderNo);
    
    
    

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(
        value = "DELETE FROM `order_detail` WHERE order_no = ?1", nativeQuery = true)
    int deleteByOrderNo(String orderNo);
    
    
        

        	  @Query("""
        	         select d
        	         from OrderDetail d
        	         where d.order.orderNo = :orderNo
        	         """)
        	  List<OrderDetail> findAllByOrderNo(@Param("orderNo") String orderNo);

        	  // 可保留的派生查詢（屬性名就是 order → orderNo）
        	  List<OrderDetail> findByOrder_OrderNo(String orderNo);
}

       
  


