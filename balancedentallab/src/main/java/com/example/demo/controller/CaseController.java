package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.CaseCreateRequest;
import com.example.demo.dto.CaseProductLine;
import com.example.demo.model.CaseEntity;
import com.example.demo.model.Clinic;
import com.example.demo.model.Dentist;
import com.example.demo.model.Member;
import com.example.demo.service.*;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class CaseController {

    @Autowired
    private CaseService srv; 
   
    @Autowired 
    private ClinicService clinicService;       
    @Autowired 
    private DentistService dentistService;     
    @Autowired 
    private MemberService memberService;   

    /** 建立案件（body 需帶 caseNo、clinic.clinicName、dentist.dentistName、createdBy.empNo 等） 
    @PostMapping
    public ResponseEntity<CaseEntity> create(@RequestBody CaseEntity payload) {
        return ResponseEntity.ok(srv.createCase(payload));
    }*/
    
 

    @PostMapping
    public ResponseEntity<?> createCase(@RequestBody CaseCreateRequest req) {
        try {
            CaseEntity entity = new CaseEntity();
            entity.setPatientName(req.getPatientName());
            if (req.getReturnAt() != null && !req.getReturnAt().isBlank()) {
                entity.setReturnAt(LocalDate.parse(req.getReturnAt()).atStartOfDay());
            }

            if (req.getClinic() == null || req.getClinic().getClinicName() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "clinic.clinicName is required"));
            if (req.getDentist() == null || req.getDentist().getDentistName() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "dentist.dentistName is required"));
            if (req.getCreatedBy() == null || req.getCreatedBy().getEmpNo() == null)
                return ResponseEntity.badRequest().body(Map.of("message", "createdBy.empNo is required"));

            Clinic  clinic  = clinicService.getClinicByName(req.getClinic().getClinicName())
                    .orElseThrow(() -> new IllegalArgumentException("診所不存在: " + req.getClinic().getClinicName()));
            Dentist dentist = dentistService.getDentistByName(req.getDentist().getDentistName())
                    .orElseThrow(() -> new IllegalArgumentException("醫師不存在: " + req.getDentist().getDentistName()));
            Member  creator = memberService.getByEmpNo(req.getCreatedBy().getEmpNo())
                    .orElseThrow(() -> new IllegalArgumentException("員工不存在: " + req.getCreatedBy().getEmpNo()));

            entity.setClinic(clinic);
            entity.setDentist(dentist);
            entity.setCreatedBy(creator);

            // ProductLineReq -> CaseProductLine 映射
            List<CaseProductLine> lines = null;
            if (req.getProducts() != null) {
                lines = req.getProducts().stream().map(p -> {
                    CaseProductLine c = new CaseProductLine();
                    c.setCategoryId(p.getCategoryId());
                    c.setItemId(p.getItemId());
                    c.setQuantity(p.getQuantity());
               
                    c.setToothCode(p.getToothCode());
                    return c;
                }).toList();
            }

            CaseEntity saved = srv.createCase(entity, lines);

            // 成功時一定要 return
            // 可回 201 Created（推薦），也可用 200 OK
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "建立案件失敗", "detail", ex.getMessage()));
        }
    }



    /** 依案件編號查單筆 */
    @GetMapping("/{caseNo}")
    public ResponseEntity<CaseEntity> findOne(@PathVariable String caseNo) {
        var c = srv.findByCaseNo(caseNo);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c);
    }

    /** 依診所名稱查列表 */
    @GetMapping("/clinic/{clinicName}")
    public List<CaseEntity> listByClinic(@PathVariable String clinicName) {
        return srv.listByClinicName(clinicName);
    }

    /** 依診所名稱 + 醫師名稱查列表 */
    @GetMapping("/clinic/{clinicName}/dentist/{dentistName}")
    public List<CaseEntity> listByClinicAndDentist(
            @PathVariable String clinicName,
            @PathVariable String dentistName) {
        return srv.listByClinicNameAndDentistName(clinicName, dentistName);
    }

    /** 取全部 */
    @GetMapping
    public List<CaseEntity> getAll() {
        return srv.getAll();
    }

    /** 更新病患名稱與寄回日（擇一或同時帶） */
    @PutMapping("/{caseNo}")
    public ResponseEntity<?> updateCase(
            @PathVariable String caseNo,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnAt) {
        boolean ok = srv.updateCase(caseNo, patientName, returnAt);
        return ok ? ResponseEntity.ok("Update Successfully") : ResponseEntity.notFound().build();
    }
    
 // JSON 版完整更新（避免變新增）
    @PutMapping(path = "/{caseNo}", consumes = "application/json")
    public ResponseEntity<?> updateCaseJson(@PathVariable String caseNo, @RequestBody CaseEntity incoming) {
        CaseEntity existed = srv.findByCaseNo(caseNo);
        if (existed == null) return ResponseEntity.notFound().build();

        // 關鍵：沿用既有 id / caseNo
        incoming.setId(existed.getId());
        incoming.setCaseNo(existed.getCaseNo());

        // 若允許更新診所/醫師/建立者，可在這裡做名稱轉實體（略）
        // 未帶則沿用舊值
        if (incoming.getClinic()==null)   incoming.setClinic(existed.getClinic());
        if (incoming.getDentist()==null)  incoming.setDentist(existed.getDentist());
        if (incoming.getCreatedBy()==null)incoming.setCreatedBy(existed.getCreatedBy());
        if (incoming.getPatientName()==null) incoming.setPatientName(existed.getPatientName());
        if (incoming.getReturnAt()==null)    incoming.setReturnAt(existed.getReturnAt());

        CaseEntity saved = srv.save(incoming);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{caseNo}")
    public ResponseEntity<Void> deleteCase(@PathVariable String caseNo) {
        CaseEntity existed = srv.findByCaseNo(caseNo);
        if (existed == null) return ResponseEntity.notFound().build();
        srv.deleteById(existed.getId());
        return ResponseEntity.noContent().build();
    }
    
    
    //以下是新增的
 // === 新增：時間篩選版（只有同時帶 receivedAtStart & receivedAtEnd 才會匹配這支）===
    @GetMapping(params = {"receivedAtStart", "receivedAtEnd"})
    public ResponseEntity<Page<CaseEntity>> listCases(
            @RequestParam(required = false) String clinicNo,
            @RequestParam(required = false) String clinicId,
            @RequestParam(required = false) String dentistNo,
            @RequestParam(required = false) String dentistId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime receivedAtStart,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime receivedAtEnd,
            Pageable pageable // 前端會送 sort=receivedAt,desc
    ) {
        Page<CaseEntity> page = srv.searchCases(
                clinicNo, clinicId, dentistNo, dentistId,
                receivedAtStart, receivedAtEnd, pageable
        );
        return ResponseEntity.ok(page);
    }


}
