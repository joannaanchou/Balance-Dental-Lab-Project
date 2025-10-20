package com.example.demo.controller;

import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.MemberService;
import com.example.demo.service.MemberService.MemberDto;
import com.example.demo.dto.ChangePasswordRequest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(
		origins= { "http://127.0.0.1:5500",
		        "http://localhost:5500",
		        "http://localhost:3000",
		        "http://localhost:8080"},
				allowCredentials="true")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    // 註冊
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Member member) {
        try {
            Member created = memberService.register(member);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // 登入
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Member member) {
        try {
            Member loggedIn = memberService.login(member.getUsername(), member.getPassword());
            return ResponseEntity.ok(loggedIn);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 變更密碼
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Member updated = memberService.changePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /** 列出所有會員 */
    @GetMapping
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /** 新增會員 */
    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memberRepository.save(member);
    }

    /** 修改會員 */
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member updatedMember) {
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setEmpNo(updatedMember.getEmpNo());
            member.setUsername(updatedMember.getUsername());
            member.setPassword(updatedMember.getPassword());
            member.setName(updatedMember.getName());
            member.setRole(updatedMember.getRole());
            member.setIsActive(updatedMember.getIsActive());
            memberRepository.save(member);
            return ResponseEntity.ok(member);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /** 刪除會員 */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable Long id) {
        if (memberRepository.existsById(id)) {
            memberRepository.deleteById(id);
            return ResponseEntity.ok("會員刪除成功");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    

    @GetMapping("/{empNo}")
    public ResponseEntity<MemberDto> getByEmpNo(@PathVariable String empNo) {
        return memberService.getSimpleByEmpNo(empNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 兼容前端另一條*/
    @GetMapping("/empNo/{empNo}")
    public ResponseEntity<MemberDto> getByEmpNoAlt(@PathVariable String empNo) {
        return getByEmpNo(empNo);
    }
}
