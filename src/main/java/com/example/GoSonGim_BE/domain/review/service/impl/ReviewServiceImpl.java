package com.example.GoSonGim_BE.domain.review.service.impl;

import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationDetailResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewWordsResponse;
import com.example.GoSonGim_BE.domain.review.exception.ReviewExceptions;
import com.example.GoSonGim_BE.domain.review.service.ReviewService;
import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import com.example.GoSonGim_BE.domain.situation.entity.SituationLog;
import com.example.GoSonGim_BE.domain.situation.repository.SituationLogRepository;
import com.example.GoSonGim_BE.global.util.PaginationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 복습 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    
    private final KitStageLogRepository kitStageLogRepository;
    private final SituationLogRepository situationLogRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    
    private static final int MAX_REVIEW_WORDS = 5;
    private static final int SAMPLE_SIZE_FOR_RANDOM = 200;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int AUDIO_URL_EXPIRATION_MINUTES = 60;
    
    @Override
    public ReviewWordsResponse getRandomReviewWords(Long userId) {
        // DB에서 랜덤으로 최대 5개 단어 조회 (최근 학습 단어 200개 범위 내에서 샘플링)
        List<String> randomWords = kitStageLogRepository.findRandomDistinctWordsByUserId(
            userId,
            SAMPLE_SIZE_FOR_RANDOM,
            MAX_REVIEW_WORDS
        );
        
        // 학습 기록이 없으면 예외 발생
        if (randomWords.isEmpty()) {
            throw new ReviewExceptions.NoLearningHistoryException();
        }
        
        return new ReviewWordsResponse(randomWords);
    }
    
    @Override
    public ReviewSituationsResponse getReviewSituations(Long userId, String category, String sort, int page, int size) {
        SituationCategory situationCategory = parseCategory(category);
        Sort sortSpec = resolveSort(sort);
        Pageable pageable = PaginationUtil.createPageable(page, size, MAX_PAGE_SIZE, sortSpec, param -> new ReviewExceptions.InvalidQueryParameterException(param));
        
        // 카테고리별 유무에 따라 다른 repository 메서드 호출
        Slice<SituationLog> pageResult = situationCategory.isAll()
            ? situationLogRepository.findLatestSituationLogsAllCategories(userId, pageable)
            : situationLogRepository.findLatestSituationLogsByCategory(userId, situationCategory, pageable);
        
        List<ReviewSituationItemResponse> items = pageResult.getContent().stream()
            .map(log -> new ReviewSituationItemResponse(
                log.getSituation().getId(),
                log.getSituation().getSituationName(),
                log.getId(),
                log.getCreatedAt()
            ))
            .toList();
        
        boolean hasNext = PaginationUtil.hasNext(pageResult);
        
        return ReviewSituationsResponse.of(items, page, size, hasNext);
    }
    
    private SituationCategory parseCategory(String category) {
        try {
            return SituationCategory.from(category);
        } catch (IllegalArgumentException e) {
            throw new ReviewExceptions.InvalidQueryParameterException("category");
        }
    }
    
    @Override
    public ReviewSituationDetailResponse getReviewSituationDetail(Long userId, Long recordingId) {
        // 학습 기록 조회
        SituationLog situationLog = situationLogRepository.findById(recordingId)
            .orElseThrow(() -> new ReviewExceptions.SituationLogNotFoundException(recordingId));
        
        // 접근 권한 확인
        if (!situationLog.getUser().getId().equals(userId)) {
            throw new ReviewExceptions.SituationLogAccessDeniedException(recordingId);
        }
        
        // 상황극 정보
        ReviewSituationDetailResponse.SituationInfo situationInfo = 
            new ReviewSituationDetailResponse.SituationInfo(
                situationLog.getSituation().getId(),
                situationLog.getSituation().getSituationName()
            );
        
        // 평가 정보
        ReviewSituationDetailResponse.EvaluationInfo evaluationInfo = 
            new ReviewSituationDetailResponse.EvaluationInfo(
                situationLog.getEvaluationScore(),
                situationLog.getEvaluationFeedback()
            );
        
        // 대화 내용 파싱 및 오디오 URL 생성
        List<ReviewSituationDetailResponse.ConversationTurn> conversation = 
            parseConversationWithAudioUrls(situationLog.getConversation());
        
        return new ReviewSituationDetailResponse(recordingId, situationInfo, evaluationInfo, conversation);
    }
    
    private List<ReviewSituationDetailResponse.ConversationTurn> parseConversationWithAudioUrls(String conversationJson) {
        try {
            if (conversationJson == null || conversationJson.isBlank() || conversationJson.equals("[]")) {
                return new ArrayList<>();
            }
            
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {};
            List<Map<String, Object>> conversationList = objectMapper.readValue(conversationJson, typeRef);
            
            return conversationList.stream()
                .map(turn -> {
                    String question = (String) turn.get("question");
                    String answerText = (String) turn.get("answer");
                    String audioFileKey = (String) turn.get("audioFileKey");
                    
                    // 오디오 URL 생성
                    String audioUrl = null;
                    Integer audioExpiresIn = AUDIO_URL_EXPIRATION_MINUTES * 60; // 초 단위로 변환
                    if (audioFileKey != null && !audioFileKey.isBlank()) {
                        try {
                            URL presignedUrl = s3Service.generateDownloadPresignedUrl(
                                audioFileKey, 
                                AUDIO_URL_EXPIRATION_MINUTES
                            );
                            audioUrl = presignedUrl.toString();
                        } catch (Exception e) {
                            log.warn("오디오 URL 생성 실패: fileKey={}", audioFileKey, e);
                        }
                    }
                    
                    ReviewSituationDetailResponse.Answer answer = 
                        new ReviewSituationDetailResponse.Answer(answerText, audioUrl, audioExpiresIn);
                    
                    return new ReviewSituationDetailResponse.ConversationTurn(question, answer);
                })
                .toList();
        } catch (Exception e) {
            log.error("대화 내역 파싱 실패: {}", conversationJson, e);
            return new ArrayList<>();
        }
    }
    
    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("latest")) {
            return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }
        if (sort.equalsIgnoreCase("oldest")) {
            return Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        }
        throw new ReviewExceptions.InvalidQueryParameterException("sort");
    }
}

