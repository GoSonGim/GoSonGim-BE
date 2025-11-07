package com.example.GoSonGim_BE.domain.review.service.impl;

import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.domain.kit.entity.KitStageLog;
import com.example.GoSonGim_BE.domain.kit.repository.KitCategoryRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitRecordsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewKitsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationItemResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewSituationsResponse;
import com.example.GoSonGim_BE.domain.review.dto.response.ReviewWordsResponse;
import com.example.GoSonGim_BE.domain.review.exception.ReviewExceptions;
import com.example.GoSonGim_BE.domain.review.service.ReviewService;
import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import com.example.GoSonGim_BE.domain.situation.entity.SituationLog;
import com.example.GoSonGim_BE.domain.situation.repository.SituationLogRepository;
import com.example.GoSonGim_BE.global.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 복습 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {
    
    private final KitStageLogRepository kitStageLogRepository;
    private final SituationLogRepository situationLogRepository;
    private final KitRepository kitRepository;
    private final KitCategoryRepository kitCategoryRepository;
    private final S3Service s3Service;

    private static final int MAX_REVIEW_WORDS = 5;
    private static final int SAMPLE_SIZE_FOR_RANDOM = 200;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int PRESIGNED_URL_EXPIRATION_MINUTES = 60;
    
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

    private SituationCategory parseCategory(String category) {
        try {
            return SituationCategory.from(category);
        } catch (IllegalArgumentException e) {
            throw new ReviewExceptions.InvalidQueryParameterException("category");
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

