package com.example.GoSonGim_BE.domain.kit.service.impl;

import com.example.GoSonGim_BE.domain.files.service.S3Service;
import com.example.GoSonGim_BE.domain.kit.dto.request.EvaluateRequest;
import com.example.GoSonGim_BE.domain.kit.dto.response.EvaluateResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitCategoriesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitStagesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitsResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.PronunciationAssessmentResponse;
import com.example.GoSonGim_BE.domain.kit.entity.Kit;
import com.example.GoSonGim_BE.domain.kit.entity.KitCategory;
import com.example.GoSonGim_BE.domain.kit.entity.KitStage;
import com.example.GoSonGim_BE.domain.kit.entity.KitStageLog;
import com.example.GoSonGim_BE.domain.kit.exception.KitExceptions;
import com.example.GoSonGim_BE.domain.kit.repository.KitCategoryRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageRepository;
import com.example.GoSonGim_BE.domain.kit.service.KitService;
import com.example.GoSonGim_BE.domain.kit.service.PronunciationAssessmentService;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KitServiceImpl implements KitService {
    
    private final KitRepository kitRepository;
    private final KitCategoryRepository kitCategoryRepository;
    private final KitStageRepository kitStageRepository;
    private final KitStageLogRepository kitStageLogRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PronunciationAssessmentService pronunciationAssessmentService;

    @Override
    public KitCategoriesResponse getKitCategories() {
        List<KitCategory> categories = kitCategoryRepository.findAll();
        
        List<KitCategoriesResponse.Category> categoryDtos = categories.stream()
            .map(category -> new KitCategoriesResponse.Category(
                category.getId(),
                category.getKitCategoryName()
            ))
            .collect(Collectors.toList());
        
        return new KitCategoriesResponse(categoryDtos.size(), categoryDtos);
    }

    @Override
    public KitsResponse getKitsByCategoryId(Long categoryId) {
        List<Kit> kits = kitRepository.findByKitCategoryId(categoryId);
        
        List<KitsResponse.Kit> kitDtos = kits.stream()
            .map(kit -> new KitsResponse.Kit(
                kit.getId(),
                kit.getKitName()
            ))
            .collect(Collectors.toList());
        
        return new KitsResponse(kitDtos.size(), kitDtos);
    }

    @Override
    public KitStagesResponse getKitStages(Long kitId) {
        Kit kit = kitRepository.findById(kitId)
            .orElseThrow(KitExceptions.KitNotFound::new);
        
        List<KitStage> stages = kit.getKitStages();
        
        List<KitStagesResponse.Stage> stageDtos = stages.stream()
            .map(stage -> new KitStagesResponse.Stage(
                stage.getId(),
                stage.getKitStageName()
            ))
            .collect(Collectors.toList());
        
        return new KitStagesResponse(
            kit.getId(),
            kit.getKitName(),
            kit.getKitCategory().getKitCategoryName(),
            stageDtos.size(),
            stageDtos
        );
    }

    @Override
    @Transactional
    public EvaluateResponse evaluatePronunciation(EvaluateRequest request, Long userId) {
        try (InputStream audioStream = s3Service.downloadFileAsStream(request.fileKey())) {
            
            // 1. Azure API 호출
            PronunciationAssessmentResponse result = pronunciationAssessmentService.assessPronunciation(
                    audioStream,
                    request.targetWord()
            );
            
            double accuracy = result.accuracy();
            double fluency = result.fluency();
            double completeness = result.completeness();
            double prosody = result.prosody();
            
            // 2. 종합 점수 및 피드백 계산
            double evaluationScore = (accuracy + fluency + completeness + prosody) / 4.0;
            boolean isSuccess = evaluationScore >= 85.0;
            
            String feedback = String.format("정확도: %.1f, 유창성: %.1f, 완성도: %.1f, 운율: %.1f", accuracy, fluency, completeness, prosody);
            
            // 3. DB 저장
            KitStage kitStage = kitStageRepository.findById(request.kitStageId())
                .orElseThrow(KitExceptions.KitStageNotFound::new);
            
            User user = userRepository.findById(userId)
                .orElseThrow(KitExceptions.UserNotFound::new);
            
            KitStageLog log = KitStageLog.builder()
                .kitStage(kitStage)
                .user(user)
                .name(null) //Todo name의 사용처
                .targetWord(request.targetWord())
                .audioFileKey(request.fileKey())
                .evaluationScore((float) evaluationScore)
                .evaluationFeedback(feedback)
                .isSuccess(isSuccess)
                .build();
            
            kitStageLogRepository.save(log);
            
            // 4. Response 생성
            return new EvaluateResponse(
                request.kitStageId(),
                request.targetWord(),
                new EvaluateResponse.PronunciationScore(
                    accuracy,
                    fluency,
                    completeness,
                    prosody
                ),
                evaluationScore,
                isSuccess
            );
            
        } catch (Exception e) {
            throw new KitExceptions.PronunciationAssessmentFailed(e.getMessage());
        }
    }

}