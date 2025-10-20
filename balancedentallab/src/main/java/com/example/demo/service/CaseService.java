package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Date; 



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class CaseService {

	@Autowired 
	private CaseRepository caseRepository;
    @Autowired 
    private ClinicRepository clinicRepo;
    @Autowired 
    private DentistRepository dentistRepo;
    @Autowired 
    private MemberRepository memberRepo;

    @Autowired 
    private OrderRepository orderRepository;
    @Autowired 
    private OrderDetailRepository orderDetailRepository;
    @Autowired 
    private ProductDetailRepository productDetailRepository;

    @PersistenceContext
    private EntityManager em;

    // 一次建立：Case + Orders + OrderDetails
    @Transactional
    public CaseEntity createCase(CaseEntity entity, List<CaseProductLine> lines) {
        if (entity == null) throw new IllegalArgumentException("payload is null");
        if (entity.getClinic() == null)  throw new IllegalArgumentException("clinic is required");
        if (entity.getDentist() == null) throw new IllegalArgumentException("dentist is required");
        if (entity.getCreatedBy() == null) throw new IllegalArgumentException("createdBy is required");

        // 病患/時間欄位
        if (entity.getPatientName() == null || entity.getPatientName().isBlank())
            throw new IllegalArgumentException("patientName is required");

        // CaseEntity.receivedAt NOT NULL，這裡若沒給就以現在時間補上
        if (entity.getReceivedAt() == null) {
            entity.setReceivedAt(LocalDateTime.now());
        }
        if (entity.getReturnAt() == null) {
            throw new IllegalArgumentException("returnAt(LocalDateTime) is required");
        }

        // 產生 caseNo 與系統欄位
        entity.setCaseNo(nextCaseNo());
        entity.setTotalAmount(BigDecimal.ZERO);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // 1) 先存 Case
        CaseEntity savedCase = caseRepository.save(entity);

        // 2) 若沒有產品，直接回傳（允許只建案不下單）
        if (lines == null || lines.isEmpty()) {
            return savedCase;
        }

        // 3) 為每個產品建立一張 Order 與一筆 OrderDetail
        BigDecimal caseTotal = BigDecimal.ZERO;

        for (CaseProductLine line : lines) {
            if (line.getCategoryId() == null || line.getItemId() == null) {
                throw new IllegalArgumentException("categoryId and itemId are required for each line");
            }
            int qty = (line.getQuantity() == null || line.getQuantity() <= 0) ? 1 : line.getQuantity();

            // 用 category.id + item.id 找 ProductDetail
            ProductDetail pd = productDetailRepository
                    .findByCategory_IdAndItem_Id(line.getCategoryId(), line.getItemId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "product_detail not found, categoryId=" + line.getCategoryId()
                                    + ", itemId=" + line.getItemId()));

            // 建立 Order
            OrderEntity order = new OrderEntity();
            order.setOrderNo(nextOrderNo());
            order.setCaseEntity(savedCase);
            order.setToothCode(line.getToothCode() == null ? null : String.valueOf(line.getToothCode()));
            order.setTotalAmount(BigDecimal.ZERO);
            OrderEntity savedOrder = orderRepository.save(order);

            // 建立 OrderDetail
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(pd);
            detail.setQty(qty);
            detail.setUnitPrice(pd.getPrice() != null ? pd.getPrice() : BigDecimal.ZERO);
            orderDetailRepository.save(detail);

            // 計算該單總額（不依賴 DB 的 line_amount）
            BigDecimal lineAmount = detail.getUnitPrice().multiply(BigDecimal.valueOf(qty));

            // 回填 Order.totalAmount
            savedOrder.setTotalAmount(lineAmount);
            orderRepository.save(savedOrder);

            // 累加至 Case 總額
            caseTotal = caseTotal.add(lineAmount);
        }

        // 4) 回填 Case.totalAmount
        savedCase.setTotalAmount(caseTotal);
        savedCase.setUpdatedAt(LocalDateTime.now());
        return caseRepository.save(savedCase);
    }

    // ====== 正式連號（每日遞增）：CASEyyyyMMdd-000001 / ORDyyyyMMdd-000001 ======

    private String nextCaseNo() {
        return nextRunningNo("CASE", "case_sequence");
    }

    private String nextOrderNo() {
        return nextRunningNo("ORD", "order_sequence");
    }

    private String nextRunningNo(String prefix, String tableName) {
        LocalDate today = LocalDate.now();

        var rows = em.createNativeQuery(
                "SELECT seq_value FROM " + tableName + " WHERE seq_date = :d FOR UPDATE")
            .setParameter("d", Date.valueOf(today))
            .getResultList();

        long next;
        if (rows.isEmpty()) {
            em.createNativeQuery(
                "INSERT INTO " + tableName + " (seq_date, seq_value) VALUES (:d, 1)")
              .setParameter("d", Date.valueOf(today))
              .executeUpdate();
            next = 1L;
        } else {
            long current = ((Number) rows.get(0)).longValue();
            next = current + 1;
            em.createNativeQuery(
                "UPDATE " + tableName + " SET seq_value = :v WHERE seq_date = :d")
              .setParameter("v", next)
              .setParameter("d", Date.valueOf(today))
              .executeUpdate();
        }

        String ymd = today.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        String seq = String.format("%06d", next);
        return prefix + ymd + "-" + seq;
    }


    public CaseEntity findByCaseNo(String caseNo) {
        return caseRepository.findByCaseNo(caseNo).orElse(null);
    }

    public List<CaseEntity> listByClinicName(String clinicName) {
        return caseRepository.findByClinicName(clinicName);
    }

    public List<CaseEntity> listByClinicNameAndDentistName(String clinicName, String dentistName) {
        return caseRepository.findByClinicNameAndDentistName(clinicName, dentistName);
    }

    public List<CaseEntity> getAll() {
        return caseRepository.findAll();
    }

    /** 更新病患名稱與寄回日（帶到哪個欄位就更新哪個） */
    @Transactional
    public boolean updateCase(String caseNo, String patientName, LocalDate returnAt) {
        CaseEntity cs = caseRepository.findByCaseNo(caseNo).orElse(null);
        if (cs == null) return false;

        if (patientName != null && !patientName.isBlank()) cs.setPatientName(patientName);
        if (returnAt != null) cs.setReturnAt(returnAt.atStartOfDay());

        cs.setUpdatedAt(LocalDateTime.now());
        caseRepository.save(cs);
        return true;
    }

    /** 重新彙總案件總額（由所有訂單的 total_amount 加總） */
    @Transactional
    public CaseEntity recalcCaseTotal(String caseNo) {
        CaseEntity cs = caseRepository.findByCaseNo(caseNo)
                .orElseThrow(() -> new NoSuchElementException("case not found: " + caseNo));

        BigDecimal sum = orderRepository.sumTotalsByCaseNo(caseNo);
        cs.setTotalAmount(sum != null ? sum : BigDecimal.ZERO);
        cs.setUpdatedAt(LocalDateTime.now());
        return caseRepository.save(cs);
    }
    
 

	 // 單純包一層 repository.save()，給 Controller 的 JSON 版 PUT 使用
	 @Transactional
	 public CaseEntity save(CaseEntity entity) {
	     return caseRepository.save(entity);
	 }
	 
	
	
	 // 依 id 刪除（Controller: DELETE /api/cases/{caseNo} 會先查到 id 再呼叫這支）
	 @Transactional
	 public void deleteById(Long id) {
	     caseRepository.deleteById(id);
	 }
	
	 // 依 caseNo 刪除
	 @Transactional
	 public boolean deleteByCaseNo(String caseNo) {
	     var opt = caseRepository.findByCaseNo(caseNo);
	     if (opt.isEmpty()) return false;
	     caseRepository.deleteById(opt.get().getId());
	     return true;
	 }
	 
	 //以下是新增的
	 public Page<CaseEntity> searchCases(
	            String clinicNo, String clinicId,
	            String dentistNo, String dentistId,
	            LocalDateTime receivedAtStart, LocalDateTime receivedAtEnd,
	            Pageable pageable) {

	        return caseRepository.searchCases(
	            emptyToNull(clinicNo),
	            emptyToNull(clinicId),
	            emptyToNull(dentistNo),
	            emptyToNull(dentistId),
	            receivedAtStart,
	            receivedAtEnd,
	            pageable
	        );
	    }

	    private static String emptyToNull(String s) {
	        return (s == null || s.isBlank()) ? null : s;
	    }

	 
	 

}
