package com.example.demo.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.CaseEntity;

public interface CaseRepository extends JpaRepository<CaseEntity, Long>{
	
	  // 依caseNo查該case
	Optional <CaseEntity>findByCaseNo(String caseNo);


	
	// 依診所名稱查所有該診所的case	
    @Query("select c from CaseEntity c where c.clinic.clinicName = :clinicName")
    List<CaseEntity> findByClinicName(@Param("clinicName") String clinicName);


	// 依診所名稱+ 醫師名稱查case
    @Query("select c from CaseEntity c where c.clinic.clinicName = :clinicName and c.dentist.dentistName = :dentistName")
    List<CaseEntity> findByClinicNameAndDentistName(@Param("clinicName") String clinicName, @Param("dentistName") String dentistName);


	
	// 加總該case底下所有訂單的總金額
	@Query(
		    value = "SELECT COALESCE(SUM(o.total_amount), 0) " +
		            "FROM `order` o " +
		            "WHERE o.case_no = ?1",
		    nativeQuery = true
		)
		
	BigDecimal sumOrderTotalsByCaseNo(String caseNo);
	
	// 直接用 repo 以 caseNo 刪除
    void deleteById(Long id);
    void deleteByCaseNo(String caseNo); 
    
    //以下是新增的
    @Query("""
            select c from CaseEntity c
            where (:clinicNo  is null or c.clinic.clinicNo   = :clinicNo)
              and (:clinicId  is null or c.clinic.id         = :clinicId)
              and (:dentistNo is null or c.dentist.dentistNo = :dentistNo)
              and (:dentistId is null or c.dentist.id        = :dentistId)
              and (:start     is null or c.receivedAt >= :start)
              and (:end       is null or c.receivedAt <= :end)
            """)
        Page<CaseEntity> searchCases(
            @Param("clinicNo")  String clinicNo,
            @Param("clinicId")  String clinicId,
            @Param("dentistNo") String dentistNo,
            @Param("dentistId") String dentistId,
            @Param("start")     LocalDateTime receivedAtStart,
            @Param("end")       LocalDateTime receivedAtEnd,
            Pageable pageable
        );
    
    //以下是1014新增的
    //以 Case 為單位 group by 診所並 sum(totalAmount)
    @Query("""
    	    SELECT c.clinic.clinicName AS clinicName,
    	           SUM(c.totalAmount) AS totalAmount
    	    FROM CaseEntity c
    	    WHERE c.receivedAt BETWEEN :start AND :end
    	    GROUP BY c.clinic.clinicName
    	    ORDER BY SUM(c.totalAmount) DESC
    	""")
    	List<Object[]> sumRevenueByClinicBetween(@Param("start") LocalDateTime start,
    	                                         @Param("end") LocalDateTime end);



}
