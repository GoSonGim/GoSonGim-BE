package com.example.GoSonGim_BE.domain.auth.repository;

import com.example.GoSonGim_BE.domain.auth.entity.UserLocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLocalCredentialRepository extends JpaRepository<UserLocalCredential, Long> {
    
    /**
     * 이메일로 로컬 자격증명 조회 (User와 함께 Fetch Join)
     */
    @Query("SELECT c FROM UserLocalCredential c JOIN FETCH c.user WHERE c.email = :email")
    Optional<UserLocalCredential> findByEmailWithUser(@Param("email") String email);
    
    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 사용자 ID로 로컬 자격증명 조회 (User와 함께 Fetch Join)
     */
    @Query("SELECT c FROM UserLocalCredential c JOIN FETCH c.user WHERE c.user.id = :userId")
    Optional<UserLocalCredential> findByUserIdWithUser(@Param("userId") Long userId);
}
