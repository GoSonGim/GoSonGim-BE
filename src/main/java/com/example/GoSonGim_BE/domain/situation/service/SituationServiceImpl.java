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
import com.example.GoSonGim_BE.domain.openai.service.OpenAIService;
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
    private final OpenAIService openAIService;
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
        
        Map<String, Object> currentTurn = conversationHistory.get(conversationHistory.size() - 1);
        String currentQuestion = (String) currentTurn.get("question");
        
        if (currentQuestion == null || currentQuestion.isBlank()) {
            throw new SituationExceptions.SessionInvalidException("현재 질문을 찾을 수 없습니다.");
        }
        
        SituationSessionReplyResponse.Evaluation evaluation = evaluateAnswer(
            situation, currentQuestion, request.answer(), session.getCurrentStep());
        
        Map<String, Object> evaluationMap = new HashMap<>();
        evaluationMap.put("isSuccess", evaluation.isSuccess());
        evaluationMap.put("score", evaluation.score());
        evaluationMap.put("feedback", evaluation.feedback());
        
        int currentTurnIndex = conversationHistory.size() - 1;
        Map<String, Object> updatedTurn = new HashMap<>();
        updatedTurn.put("turnIndex", session.getCurrentStep());
        updatedTurn.put("question", currentQuestion);
        updatedTurn.put("answer", request.answer());
        updatedTurn.put("evaluation", evaluationMap);
        conversationHistory.set(currentTurnIndex, updatedTurn);
        
        int nextTurnIndex = session.getCurrentStep() + 1;
        boolean shouldEnd = !evaluation.isSuccess() || nextTurnIndex > 5;
        
        String nextQuestion = null;
        SituationSessionReplyResponse.FinalSummary finalSummary = null;
        
        if (shouldEnd) {
            finalSummary = generateFinalSummary(situation, conversationHistory, evaluation.isSuccess());
        } else {
            nextQuestion = generateNextQuestion(situation, conversationHistory, nextTurnIndex);
            Map<String, Object> nextTurn = new HashMap<>();
            nextTurn.put("turnIndex", nextTurnIndex);
            nextTurn.put("question", nextQuestion);
            nextTurn.put("answer", null);
            nextTurn.put("evaluation", null);
            conversationHistory.add(nextTurn);
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
        
        SituationLog situationLog = SituationLog.builder()
            .situation(situation)
            .user(user)
            .aiVideoUrl(session.getHeygenUrl())
            .audioFileKey(null)
            .targetWord(null)
            .conversation(session.getConversationHistory())
            .isSuccess(finalSummary.averageScore() >= 50.0f)
            .evaluationScore(finalSummary.averageScore())
            .evaluationFeedback(finalSummary.finalFeedback())
            .build();
        
        SituationLog savedLog = situationLogRepository.save(situationLog);
        
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
}
