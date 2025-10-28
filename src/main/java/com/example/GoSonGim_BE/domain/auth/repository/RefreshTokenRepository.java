package com.example.GoSonGim_BE.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GoSonGim_BE.domain.auth.entity.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUserId(Long userId);
    Optional<RefreshToken> findByJti(String jti);
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);
}
