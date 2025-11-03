package com.example.GoSonGim_BE.domain.situation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionStartRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationDetailResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionStartResponse;
import com.example.GoSonGim_BE.domain.situation.entity.Situation;
import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import com.example.GoSonGim_BE.domain.situation.entity.SituationSession;
import com.example.GoSonGim_BE.domain.situation.exception.SituationExceptions;
import com.example.GoSonGim_BE.domain.situation.repository.SessionStorage;
import com.example.GoSonGim_BE.domain.situation.repository.SituationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SituationServiceImpl implements SituationService {

    private final SituationRepository situationRepository;
    private final SessionStorage sessionStorage;

    @Override
    @Transactional
    public SituationCreateResponse createSituation(SituationCreateRequest request) {
        Situation situation = Situation.builder()
            .situationCategory(request.situationCategory())
            .situationName(request.situationName())
            .description(request.description())
            .image(request.image())
            .build();

        Situation savedSituation = situationRepository.save(situation);
        
        return new SituationCreateResponse(
            savedSituation.getId(),
            savedSituation.getSituationCategory().name(),
            savedSituation.getSituationName(),
            savedSituation.getDescription(),
            savedSituation.getImage()
        );
    }

    @Override
    public SituationListResponse getSituationsByCategory(String category) {
        log.debug("Received category parameter: {}", category);
        SituationCategory situationCategory = SituationCategory.from(category);
        log.debug("Converted to enum: {}", situationCategory);
        
        List<Situation> situations;
        if (situationCategory.isAll()) {
            log.debug("Fetching all situations");
            situations = situationRepository.findAll();
        } else {
            log.debug("Fetching situations for category: {}", situationCategory);
            situations = situationRepository.findBySituationCategory(situationCategory);
            log.debug("Found {} situations", situations.size());
        }
        
        List<SituationListResponse.SituationItem> items = situations.stream()
            .map(situation -> new SituationListResponse.SituationItem(
                situation.getId(),
                situation.getSituationName()
            ))
            .toList();
        
        return new SituationListResponse(items);
    }

    @Override
    public SituationDetailResponse getSituationById(Long situationId) {
        Situation situation = situationRepository.findById(situationId)
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(situationId));
        
        return new SituationDetailResponse(
            situation.getId(),
            situation.getSituationName(),
            situation.getDescription(),
            situation.getImage()
        );
    }
    
    @Override
    public SituationSessionStartResponse startSession(Long userId, SituationSessionStartRequest request) {
        log.info("Starting situation session - userId: {}, situationId: {}", userId, request.situationId());
        
        // 1. 상황극 조회 (description 포함)
        Situation situation = situationRepository.findById(request.situationId())
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(request.situationId()));
        
        // 2. 세션 ID 생성 (user_{userId}_sit_{situationId}_{timestamp})
        String sessionId = String.format("user%d_sit%d_%d", 
            userId, request.situationId(), System.currentTimeMillis());
        
        // 3. 첫 질문 생성 (DB의 description 사용)
        String firstQuestion = generateFirstQuestion(situation);
        
        // 4. 세션 정보 저장 (HeyGen 정보는 프론트엔드가 직접 관리)
        SituationSession session = SituationSession.builder()
            .sessionId(sessionId)
            .userId(userId)
            .situationId(request.situationId())
            .heygenSessionId(null)  // 프론트엔드가 SDK로 생성
            .heygenAccessToken(null)
            .heygenUrl(null)
            .currentStep(1)
            .conversationHistory("[]")  // 빈 JSON 배열
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusMinutes(30))  // 30분 후 만료
            .status(SituationSession.SessionStatus.ACTIVE)
            .build();
        
        sessionStorage.save(session);
        log.info("Session saved in Redis: {}", sessionId);
        
        // 5. 응답 생성 (프론트엔드가 SDK로 HeyGen 직접 연결)
        return new SituationSessionStartResponse(
            sessionId,
            firstQuestion
        );
    }
    
    /**
     * 첫 번째 질문 생성
     */
    private String generateFirstQuestion(Situation situation) {
        // TODO: OpenAI API 연동 시 GPT-4를 사용한 동적 질문 생성
        // 상황극 설명(description)을 컨텍스트로 사용
        // 현재는 고정된 템플릿 사용
        String description = situation.getDescription() != null ? situation.getDescription() : "";
        return String.format("%s\n\n%s에 오신 것을 환영합니다. 무엇을 도와드릴까요?", 
            description, 
            situation.getSituationName());
    }
}
