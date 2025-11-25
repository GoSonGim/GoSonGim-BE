package com.example.GoSonGim_BE.domain.review.service.impl;

import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.domain.kit.entity.KitStageLog;
import com.example.GoSonGim_BE.domain.kit.repository.KitCategoryRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewDailyItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewDailyResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewMonthlyResponse;
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
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private final KitRepository kitRepository;
    private final KitCategoryRepository kitCategoryRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    private static final int MAX_REVIEW_WORDS = 5;
    private static final int SAMPLE_SIZE_FOR_RANDOM = 200;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int PRESIGNED_URL_EXPIRATION_MINUTES = 60;
    private static final int AUDIO_URL_EXPIRATION_SECONDS = 1800; // 30분
    
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
        Pageable pageable = PaginationUtil.createPageable(page, size, MAX_PAGE_SIZE, sortSpec, ReviewExceptions.InvalidQueryParameterException::new);

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

    @Override
    public ReviewKitsResponse getReviewKits(Long userId, Long categoryId, String sort, int page, int size) {
        // 카테고리 ID 검증
        if (categoryId != null && !kitCategoryRepository.existsById(categoryId)) {
            throw new ReviewExceptions.CategoryNotFoundException();
        }

        Sort sortSpec = resolveSort(sort);
        Pageable pageable = PaginationUtil.createPageable(page, size, MAX_PAGE_SIZE, sortSpec, ReviewExceptions.InvalidQueryParameterException::new);

        // 카테고리 ID 유무에 따라 다른 repository 메서드 호출
        Slice<KitStageLog> pageResult = (categoryId == null)
            ? kitStageLogRepository.findLatestKitLogsAllCategories(userId, pageable)
            : kitStageLogRepository.findLatestKitLogsByCategory(userId, categoryId, pageable);

        List<ReviewKitItemResponse> items = pageResult.getContent().stream()
            .map(log -> new ReviewKitItemResponse(
                log.getKitStage().getKit().getId(),
                log.getKitStage().getKit().getKitName(),
                log.getCreatedAt()
            ))
            .toList();

        boolean hasNext = PaginationUtil.hasNext(pageResult);

        return ReviewKitsResponse.of(items, page, size, hasNext);
    }

    @Override
    public ReviewKitRecordsResponse getKitRecords(Long userId, Long kitId) {
        // 키트 존재 여부 검증
        if (!kitRepository.existsById(kitId)) {
            throw new ReviewExceptions.KitNotFoundException();
        }

        List<KitStageLog> logs = kitStageLogRepository.findAllByUserIdAndKitId(userId, kitId);

        if (logs.isEmpty()) {
            throw new ReviewExceptions.NoLearningHistoryException();
        }

        // 첫 번째 로그에서 Kit 정보 추출
        Long responseKitId = logs.get(0).getKitStage().getKit().getId();
        String kitName = logs.get(0).getKitStage().getKit().getKitName();

        List<ReviewKitRecordItemResponse> records = logs.stream()
            .map(log -> {
                String audioUrl = null;
                if (log.getAudioFileKey() != null && !log.getAudioFileKey().isBlank()) {
                    audioUrl = s3Service.generateDownloadPresignedUrl(
                        log.getAudioFileKey(),
                        PRESIGNED_URL_EXPIRATION_MINUTES
                    ).toString();
                }

                return new ReviewKitRecordItemResponse(
                    log.getId(),
                    log.getKitStage().getId(),
                    log.getKitStage().getKitStageName(),
                    log.getEvaluationScore(),
                    log.getEvaluationFeedback(),
                    log.getIsSuccess(),
                    log.getTargetWord(),
                    audioUrl,
                    log.getCreatedAt()
                );
            })
            .toList();

        return ReviewKitRecordsResponse.of(responseKitId, kitName, records);
    }

    @Override
    public ReviewKitRecordsResponse getKitLogRecord(Long userId, Long kitStageLogId) {
        // 기준 로그 조회 및 권한 검증
        KitStageLog baseLog = kitStageLogRepository.findById(kitStageLogId)
            .orElseThrow(() -> new ReviewExceptions.KitLogNotFoundException(kitStageLogId));

        if (!baseLog.getUser().getId().equals(userId)) {
            throw new ReviewExceptions.KitLogAccessDeniedException(kitStageLogId);
        }

        // 키트 정보 추출
        Long kitId = baseLog.getKitStage().getKit().getId();
        String kitName = baseLog.getKitStage().getKit().getKitName();

        // kitId 1,2,3: 한 번의 학습 = 1개 로그
        // kitId 4 이상: 한 번의 학습 = 3개 로그
        List<KitStageLog> sessionLogs;
        if (kitId <= 3) {
            // kit 1,2,3: baseLog 하나만 반환
            sessionLogs = List.of(baseLog);
        } else {
            // kit 4 이상: 기준 로그 기준으로 같은 학습 세션의 3개 로그 조회
            sessionLogs = kitStageLogRepository.findTop3ByUserAndKitStageAndCreatedAtBefore(
                userId,
                baseLog.getKitStage().getId(),
                baseLog.getCreatedAt()
            );

            if (sessionLogs.isEmpty()) {
                throw new ReviewExceptions.NoLearningHistoryException();
            }
        }

        // 각 로그를 응답 DTO로 변환
        List<ReviewKitRecordItemResponse> records = sessionLogs.stream()
            .map(log -> {
                String audioUrl = null;
                if (log.getAudioFileKey() != null && !log.getAudioFileKey().isBlank()) {
                    audioUrl = s3Service.generateDownloadPresignedUrl(
                        log.getAudioFileKey(),
                        PRESIGNED_URL_EXPIRATION_MINUTES
                    ).toString();
                }

                return new ReviewKitRecordItemResponse(
                    log.getId(),
                    log.getKitStage().getId(),
                    log.getKitStage().getKitStageName(),
                    log.getEvaluationScore(),
                    log.getEvaluationFeedback(),
                    log.getIsSuccess(),
                    log.getTargetWord(),
                    audioUrl,
                    log.getCreatedAt()
                );
            })
            .toList();

        return ReviewKitRecordsResponse.of(kitId, kitName, records);
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
        Float rawScore = situationLog.getEvaluationScore();
        Integer truncatedScore = rawScore != null ? rawScore.intValue() : null;
        ReviewSituationDetailResponse.EvaluationInfo evaluationInfo = 
            new ReviewSituationDetailResponse.EvaluationInfo(
                truncatedScore,
                situationLog.getEvaluationFeedback()
            );
        
        // 대화 내용 파싱 및 오디오 URL 생성
        List<ReviewSituationDetailResponse.ConversationTurn> conversation = 
            parseConversationWithAudioUrls(situationLog.getConversation());
        
        return new ReviewSituationDetailResponse(recordingId, situationInfo, evaluationInfo, conversation);
    }
    
    /**
     * 대화 내역 JSON을 파싱하고 오디오 URL을 포함한 대화 턴 리스트로 변환
     */
    private List<ReviewSituationDetailResponse.ConversationTurn> parseConversationWithAudioUrls(String conversationJson) {
        if (conversationJson == null || conversationJson.isBlank() || conversationJson.equals("[]")) {
            throw new ReviewExceptions.InvalidConversationDataException("대화 내역이 비어있습니다.");
        }
        
        try {
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {};
            List<Map<String, Object>> conversationList = objectMapper.readValue(conversationJson, typeRef);
            
            return conversationList.stream()
                .map(this::mapToConversationTurn)
                .toList();
        } catch (Exception e) {
            throw new ReviewExceptions.InvalidConversationDataException("대화 내역을 불러올 수 없습니다.");
        }
    }
    
    /**
     * 대화 턴 맵을 ConversationTurn DTO로 변환
     */
    private ReviewSituationDetailResponse.ConversationTurn mapToConversationTurn(Map<String, Object> turn) {
        String question = (String) turn.get("question");
        String answerText = (String) turn.get("answer");
        String audioFileKey = (String) turn.get("audioFileKey");
        
        String audioUrl = generateAudioUrl(audioFileKey);
        ReviewSituationDetailResponse.Answer answer = 
            new ReviewSituationDetailResponse.Answer(answerText, audioUrl, AUDIO_URL_EXPIRATION_SECONDS);
        
        return new ReviewSituationDetailResponse.ConversationTurn(question, answer);
    }
    
    /**
     * 오디오 파일 키로부터 S3 presigned URL 생성
     * @return presigned URL 또는 빈 문자열 (파일 키가 없거나 생성 실패 시)
     */
    private String generateAudioUrl(String audioFileKey) {
        if (audioFileKey == null || audioFileKey.isBlank()) {
            return "";
        }
        
        try {
            URL presignedUrl = s3Service.generateDownloadPresignedUrl(
                audioFileKey, 
                AUDIO_URL_EXPIRATION_SECONDS / 60 // 분 단위로 변환
            );
            return presignedUrl.toString();
        } catch (Exception e) {
            return "";
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
    
    @Override
    public ReviewMonthlyResponse getMonthlyReview(Long userId, YearMonth month) {
        // 해당 월에 학습이 있었던 날짜 목록 조회 (java.sql.Date로 반환)
        List<Date> sqlDates = situationLogRepository.findDistinctLearningDatesByMonth(
            userId, 
            month.getYear(), 
            month.getMonthValue()
        );
        
        // java.sql.Date를 LocalDate로 변환
        List<LocalDate> learningDates = sqlDates.stream()
            .map(Date::toLocalDate)
            .toList();
        
        // 날짜(day)만 추출하여 정수 리스트로 변환
        List<Integer> days = learningDates.stream()
            .map(LocalDate::getDayOfMonth)
            .toList();
        
        // month를 yyyy-MM 형식 문자열로 변환
        String monthString = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        return new ReviewMonthlyResponse(monthString, days);
    }
    
    @Override
    public ReviewDailyResponse getDailyReview(Long userId, LocalDate date) {
        // 해당 날짜에 학습한 조음 키트 목록 조회 (중복 허용, 최신순)
        List<KitStageLog> kitLogs = kitStageLogRepository.findByUserIdAndDate(userId, date);
        
        // 해당 날짜에 학습한 상황극 목록 조회 (중복 허용, 최신순)
        List<SituationLog> situationLogs = situationLogRepository.findByUserIdAndDate(userId, date);
        
        // 조음 키트: 같은 학습 세션 내의 반복 평가는 하나만 표시, 다른 학습 세션은 모두 표시
        List<ReviewDailyItemResponse> kitItems = deduplicateKitLogsBySession(kitLogs);
        
        // 상황극을 ReviewDailyItemResponse로 변환
        List<ReviewDailyItemResponse> situationItems = situationLogs.stream()
            .map(ReviewDailyItemResponse::fromSituation)
            .toList();
        
        // 두 리스트를 합치고 최신순으로 정렬
        List<ReviewDailyItemResponse> allItems = new ArrayList<>();
        allItems.addAll(kitItems);
        allItems.addAll(situationItems);
        
        // createdAt 기준으로 최신순 정렬
        allItems.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        
        return ReviewDailyResponse.of(allItems);
    }
    
    /**
     * 조음 키트 로그를 학습 세션별로 중복 제거
     * kitId 1, 2, 3: 단계가 1개이므로 그대로 표시
     * kitId 4 이상: 같은 kit_stage_id에 대해 3번씩 평가하므로, 각 kit_stage_id의 첫 번째만 표시
     */
    private List<ReviewDailyItemResponse> deduplicateKitLogsBySession(List<KitStageLog> kitLogs) {
        // createdAt 기준으로 최신순 정렬
        List<KitStageLog> sortedLogs = kitLogs.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
        
        List<KitStageLog> deduplicatedLogs = new ArrayList<>();
        final int REPEAT_COUNT = 3; // kitId 4 이상일 때 각 stage당 반복 횟수
        
        for (int i = 0; i < sortedLogs.size(); i++) {
            KitStageLog currentLog = sortedLogs.get(i);
            Long currentKitId = currentLog.getKitStage().getKit().getId();
            
            // kitId가 1, 2, 3인 경우: 반복 평가가 없으므로 그대로 추가
            if (currentKitId <= 3) {
                deduplicatedLogs.add(currentLog);
                continue;
            }
            
            // kitId 4 이상: 같은 kit_stage_id에 대해 무조건 3개씩 들어오므로
            // 첫 번째만 추가하고 다음 2개는 건너뛰기
            Long currentKitStageId = currentLog.getKitStage().getId();
            
            // 다음 2개가 같은 kit_stage_id인지 확인
            boolean shouldSkipNext = false;
            if (i + 1 < sortedLogs.size() && i + 2 < sortedLogs.size()) {
                Long nextKitStageId1 = sortedLogs.get(i + 1).getKitStage().getId();
                Long nextKitStageId2 = sortedLogs.get(i + 2).getKitStage().getId();
                
                if (currentKitStageId.equals(nextKitStageId1) && currentKitStageId.equals(nextKitStageId2)) {
                    shouldSkipNext = true;
                }
            }
            
            deduplicatedLogs.add(currentLog);
            
            if (shouldSkipNext) {
                i += REPEAT_COUNT - 1; // 다음 2개는 건너뛰기
            }
        }
        
        return deduplicatedLogs.stream()
            .map(ReviewDailyItemResponse::fromKit)
            .toList();
    }
}

