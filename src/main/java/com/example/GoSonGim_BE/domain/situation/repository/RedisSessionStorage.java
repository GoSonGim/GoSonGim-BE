package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.SituationSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 세션 저장소
 */
@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class RedisSessionStorage implements SessionStorage {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String KEY_PREFIX = "situation:session:";
    private static final long DEFAULT_TTL_MINUTES = 30;
    
    @Override
    public void save(SituationSession session) {
        String key = KEY_PREFIX + session.getSessionId();
        
        // Redis에 저장
        redisTemplate.opsForValue().set(key, session);
        
        // TTL 설정 (만료 시간까지)
        Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
        redisTemplate.expire(key, ttl.toMinutes(), TimeUnit.MINUTES);
        
        log.debug("세션 Redis 저장: {} (TTL: {}분)", session.getSessionId(), ttl.toMinutes());
    }
    
    @Override
    public Optional<SituationSession> findById(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            log.debug("세션 없음 또는 만료됨: {}", sessionId);
            return Optional.empty();
        }
        
        SituationSession session = (SituationSession) value;
        
        // 만료 체크 (Redis TTL과 별도로)
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.debug("세션 만료됨: {}", sessionId);
            delete(sessionId);
            return Optional.empty();
        }
        
        return Optional.of(session);
    }
    
    @Override
    public void delete(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        log.debug("세션 Redis 삭제: {}", sessionId);
    }
    
    @Override
    public boolean exists(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

