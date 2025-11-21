package com.example.GoSonGim_BE.domain.situation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionEndRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionReplyRequest;
import com.example.GoSonGim_BE.domain.situation.dto.request.SituationSessionStartRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationDetailResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionEndResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionReplyResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSessionStartResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationSpeechToTextResponse;
import com.example.GoSonGim_BE.domain.openai.service.OpenAIService;
import com.example.GoSonGim_BE.domain.kit.service.PronunciationAssessmentService;
import com.example.GoSonGim_BE.domain.kit.dto.response.SpeechToTextResponse;
import org.springframework.web.multipart.MultipartFile;
import com.example.GoSonGim_BE.domain.situation.entity.Situation;
import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import com.example.GoSonGim_BE.domain.situation.entity.SituationLog;
import com.example.GoSonGim_BE.domain.situation.entity.SituationSession;
import com.example.GoSonGim_BE.domain.situation.exception.SituationExceptions;
import com.example.GoSonGim_BE.domain.situation.repository.SessionStorage;
import com.example.GoSonGim_BE.domain.situation.repository.SituationLogRepository;
import com.example.GoSonGim_BE.domain.situation.repository.SituationRepository;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.exception.UserExceptions;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;
import com.example.GoSonGim_BE.domain.users.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SituationServiceImpl implements SituationService {

    private final SituationRepository situationRepository;
    private final SessionStorage sessionStorage;
    private final SituationLogRepository situationLogRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OpenAIService openAIService;
    private final PronunciationAssessmentService pronunciationAssessmentService;
    private final ObjectMapper objectMapper;

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
        SituationCategory situationCategory = SituationCategory.from(category);
        
        List<Situation> situations = situationCategory.isAll()
            ? situationRepository.findAll()
            : situationRepository.findBySituationCategory(situationCategory);
        
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
        Situation situation = situationRepository.findById(request.situationId())
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(request.situationId()));
        
        String sessionId = String.format("user%d_sit%d_%d", 
            userId, request.situationId(), System.currentTimeMillis());
        
        String firstQuestion = generateFirstQuestion(situation);
        
        List<Map<String, Object>> initialHistory = new ArrayList<>();
        Map<String, Object> firstTurn = new HashMap<>();
        firstTurn.put("turnIndex", 1);
        firstTurn.put("question", firstQuestion);
        firstTurn.put("answer", null);
        firstTurn.put("evaluation", null);
        initialHistory.add(firstTurn);
        
        LocalDateTime now = LocalDateTime.now();
        SituationSession session = SituationSession.builder()
            .sessionId(sessionId)
            .userId(userId)
            .situationId(request.situationId())
            .heygenSessionId(null)
            .heygenAccessToken(null)
            .heygenUrl(null)
            .currentStep(1)
            .conversationHistory(serializeConversationHistoryWithEvaluation(initialHistory))
            .createdAt(now)
            .expiresAt(now.plusMinutes(30))
            .status(SituationSession.SessionStatus.ACTIVE)
            .build();
        
        sessionStorage.save(session);
        
        return new SituationSessionStartResponse(sessionId, firstQuestion);
    }
    
    @Override
    @Transactional
    public SituationSessionReplyResponse reply(Long userId, SituationSessionReplyRequest request) {
        SituationSession session = sessionStorage.findById(request.sessionId())
            .orElseThrow(() -> new SituationExceptions.SessionNotFoundException(request.sessionId()));
        
        if (!session.getUserId().equals(userId)) {
            throw new SituationExceptions.SessionAccessDeniedException(request.sessionId());
        }
        
        if (session.getStatus() != SituationSession.SessionStatus.ACTIVE) {
            throw new SituationExceptions.SessionNotActiveException(request.sessionId(), session.getStatus());
        }
        
        Situation situation = situationRepository.findById(session.getSituationId())
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(session.getSituationId()));
        
        List<Map<String, Object>> conversationHistory = parseConversationHistoryWithEvaluation(
            session.getConversationHistory());
        
        if (conversationHistory.isEmpty()) {
            throw new SituationExceptions.SessionInvalidException(
                "대화 내역이 비어있습니다. 세션을 다시 시작해주세요.");
        }
        
        // turnIndex 검증: currentStep(다시하기) 또는 이전 턴만 허용
        int currentStep = session.getCurrentStep();
        if (request.turnIndex() < 1 || request.turnIndex() > currentStep) {
            throw new SituationExceptions.SessionInvalidException(
                String.format("턴 인덱스는 1 이상 %d 이하여야 합니다. (요청된 값: %d)", 
                    currentStep, request.turnIndex()));
        }
        
        // conversationHistory에서 해당 turnIndex를 가진 turn 찾기
        Map<String, Object> targetTurn = null;
        int targetTurnIndexInList = -1;
        for (int i = 0; i < conversationHistory.size(); i++) {
            Map<String, Object> turn = conversationHistory.get(i);
            Object turnIndexObj = turn.get("turnIndex");
            if (turnIndexObj instanceof Number && ((Number) turnIndexObj).intValue() == request.turnIndex()) {
                targetTurn = turn;
                targetTurnIndexInList = i;
                break;
            }
        }
        
        if (targetTurn == null) {
            throw new SituationExceptions.SessionInvalidException(
                String.format("턴 인덱스 %d를 찾을 수 없습니다.", request.turnIndex()));
        }
        
        String currentQuestion = (String) targetTurn.get("question");
        if (currentQuestion == null || currentQuestion.isBlank()) {
            throw new SituationExceptions.SessionInvalidException("현재 질문을 찾을 수 없습니다.");
        }
        
        // 답변 평가
        SituationSessionReplyResponse.Evaluation evaluation = evaluateAnswer(
            situation, currentQuestion, request.answer(), request.turnIndex());
        
        Map<String, Object> evaluationMap = new HashMap<>();
        evaluationMap.put("isSuccess", evaluation.isSuccess());
        evaluationMap.put("score", evaluation.score());
        evaluationMap.put("feedback", evaluation.feedback());
        
        // turn 업데이트
        Map<String, Object> updatedTurn = new HashMap<>();
        updatedTurn.put("turnIndex", request.turnIndex());
        updatedTurn.put("question", currentQuestion);
        updatedTurn.put("answer", request.answer());
        updatedTurn.put("audioFileKey", request.audioFileKey()); // 오디오 파일 키 저장
        updatedTurn.put("evaluation", evaluationMap);
        conversationHistory.set(targetTurnIndexInList, updatedTurn);
        
        // 다시하기 여부 확인: turnIndex < currentStep이면 이전 턴 다시하기
        boolean isRetry = request.turnIndex() < currentStep;
        int nextTurnIndex;
        String nextQuestion = null;
        SituationSessionReplyResponse.FinalSummary finalSummary = null;
        boolean shouldEnd = false;
        
        if (isRetry) {
            // 이전 턴 다시하기: currentStep은 유지
            nextTurnIndex = currentStep;
            int nextQuestionTurnIndex = request.turnIndex() + 1;
            
            // 다시하기한 턴 이후의 모든 턴 제거
            conversationHistory.removeIf(turn -> {
                Object turnIndexObj = turn.get("turnIndex");
                return turnIndexObj instanceof Number && 
                       ((Number) turnIndexObj).intValue() > request.turnIndex();
            });
            
            // 새로운 답변 기준으로 다음 질문 생성
            nextQuestion = generateNextQuestion(situation, conversationHistory, nextQuestionTurnIndex);
            Map<String, Object> nextTurn = new HashMap<>();
            nextTurn.put("turnIndex", nextQuestionTurnIndex);
            nextTurn.put("question", nextQuestion);
            nextTurn.put("answer", null);
            nextTurn.put("evaluation", null);
            conversationHistory.add(nextTurn);
        } else {
            // 현재 턴 진행: currentStep과 같은 turnIndex
            nextTurnIndex = currentStep + 1;
            shouldEnd = nextTurnIndex > 5; // 5턴 초과 시 자동 종료
            
            // 기존 다음 질문이 있으면 제거 (새로운 답변 기준으로 재생성하기 위해)
            conversationHistory.removeIf(turn -> {
                Object turnIndexObj = turn.get("turnIndex");
                return turnIndexObj instanceof Number && 
                       ((Number) turnIndexObj).intValue() == nextTurnIndex;
            });
            
            if (shouldEnd) {
                // 세션 종료 시 최종 요약 생성 및 SituationLog 저장
                finalSummary = generateFinalSummary(situation, conversationHistory, evaluation.isSuccess());
                
                // User 조회
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
                
                // SituationLog 저장
                SituationLog situationLog = SituationLog.builder()
                    .situation(situation)
                    .user(user)
                    .conversation(serializeConversationHistoryWithEvaluation(conversationHistory))
                    .isSuccess(finalSummary.averageScore() >= 50.0f)
                    .evaluationScore(finalSummary.averageScore())
                    .evaluationFeedback(finalSummary.finalFeedback())
                    .build();
                
                situationLogRepository.save(situationLog);

                // 연속 학습일 업데이트
                userService.updateUserStreak(userId);
            } else {
                // 새로운 답변 기준으로 다음 질문 생성
                nextQuestion = generateNextQuestion(situation, conversationHistory, nextTurnIndex);
                Map<String, Object> nextTurn = new HashMap<>();
                nextTurn.put("turnIndex", nextTurnIndex);
                nextTurn.put("question", nextQuestion);
                nextTurn.put("answer", null);
                nextTurn.put("evaluation", null);
                conversationHistory.add(nextTurn);
            }
        }
        
        SituationSession updatedSession = SituationSession.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .situationId(session.getSituationId())
            .heygenSessionId(session.getHeygenSessionId())
            .heygenAccessToken(session.getHeygenAccessToken())
            .heygenUrl(session.getHeygenUrl())
            .currentStep(nextTurnIndex)
            .conversationHistory(serializeConversationHistoryWithEvaluation(conversationHistory))
            .createdAt(session.getCreatedAt())
            .expiresAt(session.getExpiresAt())
            .status(shouldEnd ? SituationSession.SessionStatus.COMPLETED : session.getStatus())
            .build();
        
        sessionStorage.save(updatedSession);
        
        return new SituationSessionReplyResponse(
            evaluation, nextQuestion, nextTurnIndex, shouldEnd, finalSummary);
    }
    
    private String getDescription(Situation situation) {
        return situation.getDescription() != null ? situation.getDescription() : "";
    }
    
    private String generateFirstQuestion(Situation situation) {
        return openAIService.generateFirstQuestion(getDescription(situation), situation.getSituationName());
    }
    
    private SituationSessionReplyResponse.Evaluation evaluateAnswer(
            Situation situation, String question, String answer, Integer turnIndex) {
        OpenAIService.EvaluationResult result = openAIService.evaluateAnswer(
            getDescription(situation), question, answer, turnIndex);
        return new SituationSessionReplyResponse.Evaluation(
            result.isSuccess(), result.feedback(), result.score());
    }
    
    private String generateNextQuestion(Situation situation, List<Map<String, Object>> conversationHistory, int turnIndex) {
        return openAIService.generateNextQuestion(getDescription(situation), conversationHistory, turnIndex);
    }
    
    private SituationSessionReplyResponse.FinalSummary generateFinalSummary(
            Situation situation, List<Map<String, Object>> conversationHistory, boolean lastEvaluationSuccess) {
        float averageScore = calculateAverageScore(conversationHistory);
        String finalFeedback = openAIService.generateFinalFeedback(
            getDescription(situation), averageScore, lastEvaluationSuccess);
        
        return new SituationSessionReplyResponse.FinalSummary(averageScore, finalFeedback);
    }
    
    private float calculateAverageScore(List<Map<String, Object>> conversationHistory) {
        float totalScore = 0.0f;
        int count = 0;
        
        for (Map<String, Object> turn : conversationHistory) {
            @SuppressWarnings("unchecked")
            Map<String, Object> evaluation = (Map<String, Object>) turn.get("evaluation");
            if (evaluation != null && evaluation.get("score") instanceof Number scoreObj) {
                totalScore += scoreObj.floatValue();
                count++;
            }
        }
        
        return count > 0 ? totalScore / count : 0.0f;
    }
    
    private List<Map<String, Object>> parseConversationHistoryWithEvaluation(String historyJson) {
        try {
            if (historyJson == null || historyJson.isBlank() || historyJson.equals("[]")) {
                return new ArrayList<>();
            }
            TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(historyJson, typeRef);
        } catch (Exception e) {
            log.warn("대화 내역 파싱 실패: {}", historyJson, e);
            return new ArrayList<>();
        }
    }
    
    private String serializeConversationHistoryWithEvaluation(List<Map<String, Object>> history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (Exception e) {
            log.error("대화 내역 직렬화 실패", e);
            return "[]";
        }
    }
    
    @Override
    @Transactional
    public SituationSessionEndResponse endSession(Long userId, SituationSessionEndRequest request) {
        SituationSession session = sessionStorage.findById(request.sessionId())
            .orElseThrow(() -> new SituationExceptions.SessionNotFoundException(request.sessionId()));
        
        if (!session.getUserId().equals(userId)) {
            throw new SituationExceptions.SessionAccessDeniedException(request.sessionId());
        }
        
        if (session.getStatus() == SituationSession.SessionStatus.COMPLETED) {
            throw new SituationExceptions.SessionInvalidException("이미 종료된 세션입니다.");
        }
        
        Situation situation = situationRepository.findById(session.getSituationId())
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(session.getSituationId()));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        List<Map<String, Object>> conversationHistory = parseConversationHistoryWithEvaluation(
            session.getConversationHistory());
        
        if (conversationHistory.isEmpty()) {
            throw new SituationExceptions.SessionInvalidException(
                "대화 내역이 비어있습니다. 세션을 다시 시작해주세요.");
        }
        
        boolean lastEvaluationSuccess = conversationHistory.stream()
            .filter(turn -> turn.get("evaluation") != null)
            .map(turn -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> evaluation = (Map<String, Object>) turn.get("evaluation");
                return Boolean.TRUE.equals(evaluation.get("isSuccess"));
            })
            .reduce((first, second) -> second)
            .orElse(false);
        
        SituationSessionReplyResponse.FinalSummary finalSummary = generateFinalSummary(
            situation, conversationHistory, lastEvaluationSuccess);
        
        // SituationLog 저장
        // conversationHistory에 이미 모든 turn의 audioFileKey가 포함되어 있음
        SituationLog situationLog = SituationLog.builder()
            .situation(situation)
            .user(user)
            .conversation(session.getConversationHistory())
            .isSuccess(finalSummary.averageScore() >= 50.0f)
            .evaluationScore(finalSummary.averageScore())
            .evaluationFeedback(finalSummary.finalFeedback())
            .build();
        
        SituationLog savedLog = situationLogRepository.save(situationLog);

        // 연속 학습일 업데이트
        userService.updateUserStreak(userId);

        SituationSession completedSession = SituationSession.builder()
            .sessionId(session.getSessionId())
            .userId(session.getUserId())
            .situationId(session.getSituationId())
            .heygenSessionId(session.getHeygenSessionId())
            .heygenAccessToken(session.getHeygenAccessToken())
            .heygenUrl(session.getHeygenUrl())
            .currentStep(session.getCurrentStep())
            .conversationHistory(session.getConversationHistory())
            .createdAt(session.getCreatedAt())
            .expiresAt(session.getExpiresAt())
            .status(SituationSession.SessionStatus.COMPLETED)
            .build();
        
        sessionStorage.save(completedSession);
        
        return new SituationSessionEndResponse(savedLog.getId(), finalSummary);
    }
    
    @Override
    public SituationSpeechToTextResponse transcribeAudio(MultipartFile audioFile) {
        try {
            SpeechToTextResponse result = pronunciationAssessmentService.transcribe(audioFile.getInputStream());
            
            return new SituationSpeechToTextResponse(
                result.recognizedText(),
                result.confidence()
            );
        } catch (Exception e) {
            throw new SituationExceptions.SpeechToTextException("음성 인식에 실패했습니다.");
        }
    }
}
