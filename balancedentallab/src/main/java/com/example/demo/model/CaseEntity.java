package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`case`")

public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="case_no", unique = true, nullable = false, length = 40)
    private String caseNo;

    @ManyToOne(optional = false)
    @JoinColumn(name="clinic_no", referencedColumnName = "clinic_no")
    private Clinic clinic;

    @ManyToOne(optional = false)
    @JoinColumn(name="dentist_no", referencedColumnName = "dentist_no")
    private Dentist dentist;

    @Column(name="patient_name", length = 120)
    private String patientName;

    @Column(name="received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name="return_at")
    private LocalDateTime returnAt;

    @Column(name="total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    @JoinColumn(name="created_by", referencedColumnName = "emp_no")
    private Member createdBy;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

	@Override
	public String toString() {
		return "CaseEntity [id=" + id + ", caseNo=" + caseNo + ", patientName=" + patientName + ", receivedAt="
				+ receivedAt + ", returnAt=" + returnAt + ", totalAmount=" + totalAmount + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}
    
    
    
}
