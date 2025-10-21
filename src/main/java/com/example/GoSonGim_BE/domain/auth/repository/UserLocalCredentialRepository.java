package com.example.GoSonGim_BE.domain.auth.repository;

import com.example.GoSonGim_BE.domain.auth.entity.UserLocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLocalCredentialRepository extends JpaRepository<UserLocalCredential, Long> {
    Optional<UserLocalCredential> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserLocalCredential> findByUserId(Long userId);
}
