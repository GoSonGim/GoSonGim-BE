package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.SituationSession;

import java.util.Optional;

/**
 * 세션 저장소 인터페이스
 * 구현체: InMemorySessionStorage (현재), RedisSessionStorage (향후)
 */
public interface SessionStorage {
    
    /**
     * 세션 저장
     */
    void save(SituationSession session);
    
    /**
     * 세션 ID로 조회
     */
    Optional<SituationSession> findById(String sessionId);
    
    /**
     * 세션 삭제
     */
    void delete(String sessionId);
    
    /**
     * 세션 존재 여부 확인
     */
    boolean exists(String sessionId);
}

