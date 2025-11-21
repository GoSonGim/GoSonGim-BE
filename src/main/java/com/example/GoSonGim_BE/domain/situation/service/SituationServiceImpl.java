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
        // 세션 검증 및 조회
        SituationSession session = validateAndGetSession(userId, request.sessionId());
        Situation situation = situationRepository.findById(session.getSituationId())
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(session.getSituationId()));
        
        // 대화 내역 파싱 및 검증
        List<Map<String, Object>> conversationHistory = parseConversationHistoryWithEvaluation(
            session.getConversationHistory());
        
        if (conversationHistory.isEmpty()) {
            throw new SituationExceptions.SessionInvalidException(
                "대화 내역이 비어있습니다. 세션을 다시 시작해주세요.");
        }
        
        // turnIndex 검증 및 turn 찾기
        int currentStep = session.getCurrentStep();
        validateTurnIndex(request.turnIndex(), currentStep);
        
        TurnInfo turnInfo = findTurnByIndex(conversationHistory, request.turnIndex());
        
        // 답변 평가 및 turn 업데이트
        SituationSessionReplyResponse.Evaluation evaluation = evaluateAnswer(
            situation, turnInfo.question(), request.answer(), request.turnIndex());
        
        updateTurnWithEvaluation(conversationHistory, turnInfo.indexInList(), 
            request, turnInfo.question(), evaluation);
        
        // 다시하기 또는 다음 질문 처리
        boolean isRetry = request.turnIndex() < currentStep;
        ProcessingResult result = isRetry
            ? processRetry(situation, conversationHistory, currentStep, request.turnIndex(), evaluation)
            : processNextTurn(userId, situation, conversationHistory, currentStep, evaluation);
        
        // 세션 업데이트 및 저장
        SituationSession updatedSession = buildUpdatedSession(session, result.nextTurnIndex(), 
            conversationHistory, result.shouldEnd());
        sessionStorage.save(updatedSession);
        
        return new SituationSessionReplyResponse(
            evaluation, result.nextQuestion(), result.nextTurnIndex(), result.shouldEnd(), result.finalSummary());
    }
    
    /**
     * 세션 검증 및 조회
     */
    private SituationSession validateAndGetSession(Long userId, String sessionId) {
        SituationSession session = sessionStorage.findById(sessionId)
            .orElseThrow(() -> new SituationExceptions.SessionNotFoundException(sessionId));
        
        if (!session.getUserId().equals(userId)) {
            throw new SituationExceptions.SessionAccessDeniedException(sessionId);
        }
        
        if (session.getStatus() != SituationSession.SessionStatus.ACTIVE) {
            throw new SituationExceptions.SessionNotActiveException(sessionId, session.getStatus());
        }
        
        return session;
    }
    
    /**
     * turnIndex 검증
     */
    private void validateTurnIndex(int turnIndex, int currentStep) {
        if (turnIndex < 1 || turnIndex > currentStep) {
            throw new SituationExceptions.SessionInvalidException(
                String.format("턴 인덱스는 1 이상 %d 이하여야 합니다. (요청된 값: %d)", 
                    currentStep, turnIndex));
        }
    }
    
    /**
     * conversationHistory에서 turnIndex로 turn 찾기
     */
    private TurnInfo findTurnByIndex(List<Map<String, Object>> conversationHistory, int turnIndex) {
        for (int i = 0; i < conversationHistory.size(); i++) {
            Map<String, Object> turn = conversationHistory.get(i);
            Object turnIndexObj = turn.get("turnIndex");
            if (turnIndexObj instanceof Number && ((Number) turnIndexObj).intValue() == turnIndex) {
                String question = (String) turn.get("question");
                if (question == null || question.isBlank()) {
                    throw new SituationExceptions.SessionInvalidException("현재 질문을 찾을 수 없습니다.");
                }
                return new TurnInfo(i, question);
            }
        }
        
        throw new SituationExceptions.SessionInvalidException(
            String.format("턴 인덱스 %d를 찾을 수 없습니다.", turnIndex));
    }
    
    /**
     * turn에 답변과 평가 업데이트
     */
    private void updateTurnWithEvaluation(
            List<Map<String, Object>> conversationHistory,
            int turnIndexInList,
            SituationSessionReplyRequest request,
            String question,
            SituationSessionReplyResponse.Evaluation evaluation) {
        
        Map<String, Object> evaluationMap = new HashMap<>();
        evaluationMap.put("isSuccess", evaluation.isSuccess());
        evaluationMap.put("score", evaluation.score());
        evaluationMap.put("feedback", evaluation.feedback());
        
        Map<String, Object> updatedTurn = new HashMap<>();
        updatedTurn.put("turnIndex", request.turnIndex());
        updatedTurn.put("question", question);
        updatedTurn.put("answer", request.answer());
        updatedTurn.put("audioFileKey", request.audioFileKey());
        updatedTurn.put("evaluation", evaluationMap);
        
        conversationHistory.set(turnIndexInList, updatedTurn);
    }
    
    /**
     * 다시하기 처리
     */
    private ProcessingResult processRetry(
            Situation situation,
            List<Map<String, Object>> conversationHistory,
            int currentStep,
            int retryTurnIndex,
            SituationSessionReplyResponse.Evaluation evaluation) {
        
        int nextQuestionTurnIndex = retryTurnIndex + 1;
        
        // 다시하기한 턴 이후의 모든 턴 제거
        removeTurnsAfter(conversationHistory, retryTurnIndex);
        
        // 새로운 답변 기준으로 다음 질문 생성 및 추가
        String nextQuestion = generateNextQuestion(situation, conversationHistory, nextQuestionTurnIndex);
        addNewTurn(conversationHistory, nextQuestionTurnIndex, nextQuestion);
        
        return new ProcessingResult(currentStep, nextQuestion, false, null);
    }
    
    /**
     * 다음 질문으로 진행
     */
    private ProcessingResult processNextTurn(
            Long userId,
            Situation situation,
            List<Map<String, Object>> conversationHistory,
            int currentStep,
            SituationSessionReplyResponse.Evaluation evaluation) {
        
        int nextTurnIndex = currentStep + 1;
        boolean shouldEnd = nextTurnIndex > 5;
        
        // 기존 다음 질문 제거
        removeTurnAt(conversationHistory, nextTurnIndex);
        
        if (shouldEnd) {
            SituationSessionReplyResponse.FinalSummary finalSummary = 
                generateFinalSummary(situation, conversationHistory, evaluation.isSuccess());
            saveSessionLogAndUpdateStreak(userId, situation, conversationHistory, finalSummary);
            return new ProcessingResult(nextTurnIndex, null, true, finalSummary);
        } else {
            String nextQuestion = generateNextQuestion(situation, conversationHistory, nextTurnIndex);
            addNewTurn(conversationHistory, nextTurnIndex, nextQuestion);
            return new ProcessingResult(nextTurnIndex, nextQuestion, false, null);
        }
    }
    
    /**
     * 특정 턴 이후의 모든 턴 제거
     */
    private void removeTurnsAfter(List<Map<String, Object>> conversationHistory, int turnIndex) {
        conversationHistory.removeIf(turn -> {
            Object turnIndexObj = turn.get("turnIndex");
            return turnIndexObj instanceof Number && 
                   ((Number) turnIndexObj).intValue() > turnIndex;
        });
    }
    
    /**
     * 특정 턴 제거
     */
    private void removeTurnAt(List<Map<String, Object>> conversationHistory, int turnIndex) {
        conversationHistory.removeIf(turn -> {
            Object turnIndexObj = turn.get("turnIndex");
            return turnIndexObj instanceof Number && 
                   ((Number) turnIndexObj).intValue() == turnIndex;
        });
    }
    
    /**
     * conversationHistory에 새로운 turn 추가
     */
    private void addNewTurn(List<Map<String, Object>> conversationHistory, int turnIndex, String question) {
        Map<String, Object> nextTurn = new HashMap<>();
        nextTurn.put("turnIndex", turnIndex);
        nextTurn.put("question", question);
        nextTurn.put("answer", null);
        nextTurn.put("evaluation", null);
        conversationHistory.add(nextTurn);
    }
    
    /**
     * 세션 로그 저장 및 연속 학습일 업데이트
     */
    private void saveSessionLogAndUpdateStreak(
            Long userId,
            Situation situation,
            List<Map<String, Object>> conversationHistory,
            SituationSessionReplyResponse.FinalSummary finalSummary) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        SituationLog situationLog = SituationLog.builder()
            .situation(situation)
            .user(user)
            .conversation(serializeConversationHistoryWithEvaluation(conversationHistory))
            .isSuccess(finalSummary.averageScore() >= 50.0f)
            .evaluationScore(finalSummary.averageScore())
            .evaluationFeedback(finalSummary.finalFeedback())
            .build();
        
        situationLogRepository.save(situationLog);
        userService.updateUserStreak(userId);
    }
    
    /**
     * 업데이트된 세션 빌드
     */
    private SituationSession buildUpdatedSession(
            SituationSession session,
            int nextTurnIndex,
            List<Map<String, Object>> conversationHistory,
            boolean shouldEnd) {
        
        return SituationSession.builder()
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
    }
    
    /**
     * Turn 정보 (리스트 인덱스와 질문)
     */
    private record TurnInfo(int indexInList, String question) {}
    
    /**
     * 처리 결과
     */
    private record ProcessingResult(
        int nextTurnIndex,
        String nextQuestion,
        boolean shouldEnd,
        SituationSessionReplyResponse.FinalSummary finalSummary
    ) {}
    
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
        // 세션 검증 및 조회
        SituationSession session = validateAndGetSessionForEnd(userId, request.sessionId());
        
        Situation situation = situationRepository.findById(session.getSituationId())
            .orElseThrow(() -> new SituationExceptions.SituationNotFoundException(session.getSituationId()));
        
        // 대화 내역 파싱 및 검증
        List<Map<String, Object>> conversationHistory = parseConversationHistoryWithEvaluation(
            session.getConversationHistory());
        
        if (conversationHistory.isEmpty()) {
            throw new SituationExceptions.SessionInvalidException(
                "대화 내역이 비어있습니다. 세션을 다시 시작해주세요.");
        }
        
        // 마지막 평가 성공 여부 확인
        boolean lastEvaluationSuccess = getLastEvaluationSuccess(conversationHistory);
        
        // 최종 요약 생성
        SituationSessionReplyResponse.FinalSummary finalSummary = generateFinalSummary(
            situation, conversationHistory, lastEvaluationSuccess);
        
        // SituationLog 저장 및 세션 완료 처리
        Long logId = saveSessionLogAndCompleteSession(userId, session, situation, finalSummary);
        
        return new SituationSessionEndResponse(logId, finalSummary);
    }
    
    /**
     * 세션 종료를 위한 검증 (COMPLETED 체크 포함)
     */
    private SituationSession validateAndGetSessionForEnd(Long userId, String sessionId) {
        SituationSession session = sessionStorage.findById(sessionId)
            .orElseThrow(() -> new SituationExceptions.SessionNotFoundException(sessionId));
        
        if (!session.getUserId().equals(userId)) {
            throw new SituationExceptions.SessionAccessDeniedException(sessionId);
        }
        
        if (session.getStatus() == SituationSession.SessionStatus.COMPLETED) {
            throw new SituationExceptions.SessionInvalidException("이미 종료된 세션입니다.");
        }
        
        return session;
    }
    
    /**
     * 마지막 평가 성공 여부 확인
     */
    private boolean getLastEvaluationSuccess(List<Map<String, Object>> conversationHistory) {
        return conversationHistory.stream()
            .filter(turn -> turn.get("evaluation") != null)
            .map(turn -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> evaluation = (Map<String, Object>) turn.get("evaluation");
                return Boolean.TRUE.equals(evaluation.get("isSuccess"));
            })
            .reduce((first, second) -> second)
            .orElse(false);
    }
    
    /**
     * SituationLog 저장 및 세션 완료 처리
     */
    private Long saveSessionLogAndCompleteSession(
            Long userId,
            SituationSession session,
            Situation situation,
            SituationSessionReplyResponse.FinalSummary finalSummary) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        // SituationLog 저장
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
        
        // 세션 완료 처리
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
        
        return savedLog.getId();
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
