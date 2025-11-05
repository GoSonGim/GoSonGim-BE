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
    
    @Override
    public void save(SituationSession session) {
        String key = KEY_PREFIX + session.getSessionId();
        redisTemplate.opsForValue().set(key, session);
        
        Duration ttl = Duration.between(LocalDateTime.now(), session.getExpiresAt());
        redisTemplate.expire(key, ttl.toMinutes(), TimeUnit.MINUTES);
    }
    
    @Override
    public Optional<SituationSession> findById(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return Optional.empty();
        }
        
        SituationSession session;
        if (value instanceof SituationSession) {
            session = (SituationSession) value;
        } else {
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.datatype.jsr310.JavaTimeModule javaTimeModule = new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();
                objectMapper.registerModule(javaTimeModule);
                objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                
                String json = objectMapper.writeValueAsString(value);
                session = objectMapper.readValue(json, SituationSession.class);
            } catch (Exception e) {
                log.error("세션 변환 실패: {}", sessionId, e);
                return Optional.empty();
            }
        }
        
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            delete(sessionId);
            return Optional.empty();
        }
        
        return Optional.of(session);
    }
    
    @Override
    public void delete(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
    
    @Override
    public boolean exists(String sessionId) {
        String key = KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

