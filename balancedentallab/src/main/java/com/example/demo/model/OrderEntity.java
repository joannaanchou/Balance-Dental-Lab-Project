package com.example.demo.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`order`")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_no", unique = true, nullable = false, length = 40)
    private String orderNo;

    @ManyToOne(optional = false)
    @JoinColumn(name="case_no", referencedColumnName = "case_no")
    private CaseEntity caseEntity;

    @Column(name="tooth_code", length = 10)
    private String toothCode;

    @Column(name="total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

	@Override
	public String toString() {
		return "OrderEntity [id=" + id + ", orderNo=" + orderNo + ", caseEntity=" + caseEntity + ", toothCode="
				+ toothCode + ", totalAmount=" + totalAmount + "]";
	}
    
    
}
