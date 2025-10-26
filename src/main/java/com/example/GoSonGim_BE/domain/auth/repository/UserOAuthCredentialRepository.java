package com.example.GoSonGim_BE.domain.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.GoSonGim_BE.domain.auth.entity.UserOAuthCredential;

@Repository
public interface UserOAuthCredentialRepository extends JpaRepository<UserOAuthCredential, Long> {
    Optional<UserOAuthCredential> findByProviderAndProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
}
