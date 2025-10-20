package com.example.demo.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name="order_no", referencedColumnName = "order_no")
    private OrderEntity order;

    @ManyToOne(optional = false)
    @JoinColumn(name="product_no", referencedColumnName = "product_no")
    private ProductDetail product;

    @Column(nullable = false)
    private Integer qty = 1;

    @Column(name="unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name="line_amount", insertable = false, updatable = false, precision = 14, scale = 2)
    private BigDecimal lineAmount;

	@Override
	public String toString() {
		return "OrderDetail [id=" + id + ", order=" + order + ", product=" + product + ", qty=" + qty + ", unitPrice="
				+ unitPrice + ", lineAmount=" + lineAmount + "]";
	}
    
    
   
}



