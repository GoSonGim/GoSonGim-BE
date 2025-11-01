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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
    public EvaluateResponse evaluatePronunciation(List<EvaluateRequest> evaluations, Long userId) {
        try {
            // 1. 유효성 검사 - 사용자 존재 확인
            User user = userRepository.findById(userId)
                .orElseThrow(KitExceptions.UserNotFound::new);
            
            // 2. 각 파일에 대해 개별 평가 수행
            List<EvaluateResponse.IndividualResult> individualResults = new ArrayList<>();
            
            for (EvaluateRequest evaluation : evaluations) {
                Long kitStageId = evaluation.kitStageId();
                String fileKey = evaluation.fileKey();
                String targetWord = evaluation.targetWord();
                
                // 각 evaluation마다 KitStage 검증
                KitStage kitStage = kitStageRepository.findById(kitStageId)
                    .orElseThrow(KitExceptions.KitStageNotFound::new);
                
                try (InputStream audioStream = s3Service.downloadFileAsStream(fileKey)) {
                    
                    // Azure API 호출
                    PronunciationAssessmentResponse result = pronunciationAssessmentService.assessPronunciation(
                            audioStream,
                            targetWord
                    );
                    
                    double accuracy = result.accuracy();
                    double fluency = result.fluency();
                    double completeness = result.completeness();
                    double prosody = result.prosody();
                    
                    // 개별 점수 계산
                    double evaluationScore = (accuracy + fluency + completeness + prosody) / 4.0;
                    boolean isSuccess = evaluationScore >= 85.0;
                    
                    String feedback = String.format("정확도: %.1f, 유창성: %.1f, 완성도: %.1f, 운율: %.1f", accuracy, fluency, completeness, prosody);
                    
                    // DB에 개별 로그 저장
                    KitStageLog log = KitStageLog.builder()
                        .kitStage(kitStage)
                        .user(user)
                        .targetWord(targetWord)
                        .audioFileKey(fileKey)
                        .evaluationScore((float) evaluationScore)
                        .evaluationFeedback(feedback)
                        .isSuccess(isSuccess)
                        .build();
                    
                    kitStageLogRepository.save(log);
                    
                    // 개별 결과 추가
                    individualResults.add(new EvaluateResponse.IndividualResult(
                        kitStageId,
                        targetWord,
                        result.recognizedText(),
                        new EvaluateResponse.PronunciationScore(accuracy, fluency, completeness, prosody),
                        evaluationScore,
                        isSuccess
                    ));
                    
                } catch (Exception e) {
                    log.error("파일 평가 실패: {} (kitStageId: {})", fileKey, kitStageId, e);
                    // 실패한 파일도 결과에 포함 (0점으로 처리)
                    individualResults.add(new EvaluateResponse.IndividualResult(
                        kitStageId,
                        targetWord,
                        "평가 실패: " + e.getMessage(),
                        new EvaluateResponse.PronunciationScore(0.0, 0.0, 0.0, 0.0),
                        0.0,
                        false
                    ));
                }
            }
            
            // 3. 종합 평가 계산
            EvaluateResponse.OverallResult overallResult = calculateOverallResult(individualResults);
            
            // 4. Response 생성
            return new EvaluateResponse(
                individualResults,
                overallResult
            );
            
        } catch (Exception e) {
            throw new KitExceptions.PronunciationAssessmentFailed(e.getMessage());
        }
    }
    
    private EvaluateResponse.OverallResult calculateOverallResult(List<EvaluateResponse.IndividualResult> individualResults) {
        // 전체 평균 점수 계산 (모든 파일 대상)
        double overallScore = individualResults.stream()
            .mapToDouble(EvaluateResponse.IndividualResult::evaluationScore)
            .average()
            .orElse(0.0);
        
        // 소수점 한자리수까지 반올림
        overallScore = Math.round(overallScore * 10.0) / 10.0;
        
        String overallFeedback = generateOverallFeedback(overallScore);
        
        return new EvaluateResponse.OverallResult(overallScore, overallFeedback);
    }
    
    private String generateOverallFeedback(double score) {
        if (score >= 90) {
            return "매우 우수한 발음입니다! 정확하고 자연스러운 발음으로 한국어를 구사하고 계시네요. " +
                   "이 수준의 발음 실력이라면 일상 대화에서 전혀 문제가 없을 것 같습니다. " +
                   "계속해서 다양한 단어와 문장으로 연습하시면서 현재의 수준을 유지해 주세요. " +
                   "정말 훌륭한 결과입니다!";
        } else if (score >= 80) {
            return "우수한 발음 실력을 보여주고 계십니다! 대부분의 단어를 정확하게 발음하고 있어서 " +
                   "의사소통에 큰 어려움이 없을 것 같아요. 조금만 더 연습하시면 원어민 수준의 " +
                   "발음에 가까워질 수 있을 것 같습니다. 특히 어려운 발음들도 꾸준히 연습해보시면 " +
                   "더욱 자연스러운 발음을 구사할 수 있을 거예요. 현재 수준도 정말 좋습니다!";
        } else if (score >= 70) {
            return "양호한 발음 수준입니다! 기본적인 발음은 잘 되고 있고, 상대방이 충분히 알아들을 수 있는 " +
                   "수준이에요. 몇 가지 발음 부분을 조금 더 연습하시면 더욱 향상될 것 같습니다. " +
                   "꾸준한 연습을 통해 어려운 발음들도 정복해보세요. 지금도 충분히 잘하고 계시니까 " +
                   "자신감을 가지시고 계속 연습해주시면 됩니다!";
        } else if (score >= 60) {
            return "보통 수준의 발음입니다. 기본적인 의사소통은 가능하지만, 조금 더 정확한 발음을 위해 " +
                   "연습이 필요해 보여요. 천천히 또박또박 말하는 연습을 해보시고, 특히 어려운 발음들은 " +
                   "반복 연습을 통해 개선해보세요. 포기하지 마시고 꾸준히 연습하시면 분명 향상될 거예요. " +
                   "지금 이 순간도 발전하고 있는 과정이니까 응원합니다!";
        } else if (score >= 50) {
            return "개선이 필요한 수준입니다. 하지만 걱정하지 마세요! 발음은 연습을 통해 충분히 향상될 수 있어요. " +
                   "천천히 정확하게 발음하는 연습부터 시작해보시고, 원어민의 발음을 많이 들으면서 따라하는 " +
                   "연습을 해보세요. 매일 조금씩이라도 꾸준히 연습하시면 분명 좋은 결과가 있을 거예요. " +
                   "포기하지 마시고 차근차근 연습해나가시길 응원합니다!";
        } else {
            return "더 많은 연습이 필요한 상황이지만, 지금 시작하고 계시는 것 자체가 훌륭합니다! " +
                   "발음은 하루아침에 늘지 않지만, 꾸준한 연습을 통해 반드시 향상될 수 있어요. " +
                   "기초적인 발음부터 천천히 정확하게 연습해보시고, 매일 조금씩이라도 시간을 내어 " +
                   "연습해보세요. 지금은 어렵게 느껴질 수 있지만, 몇 개월 후에는 분명 놀라운 변화를 " +
                   "경험하실 수 있을 거예요. 화이팅!";
        }
    }

}