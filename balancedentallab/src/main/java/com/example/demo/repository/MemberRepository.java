package com.example.demo.repository;

import com.example.demo.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmpNo(String empNo);
    Optional<Member> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmpNo(String empNo);

    // 找出最大 empNo 的會員（自動只取一筆）
    Optional<Member> findTopByOrderByEmpNoDesc();
    
 
    

}
