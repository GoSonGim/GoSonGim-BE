package com.example.GoSonGim_BE.domain.openai.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.GoSonGim_BE.domain.openai.exception.OpenAIExceptions;
import com.example.GoSonGim_BE.global.config.OpenAIProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {
    
    private final OpenAIProperties openAIProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String CHAT_ENDPOINT = "/chat/completions";
    
    @Override
    public String generateFirstQuestion(String situationDescription, String situationName) {
        String systemPrompt = String.format(
            "당신은 조음 장애 학습자를 위한 상황극 대화 상대입니다.\n\n" +
            "상황 설명: %s\n\n" +
            "중요한 규칙:\n" +
            "1. 상황 설명을 분석하여 학습자의 역할과 상대방의 역할을 파악하세요\n" +
            "   - 학습자가 \"~하려고 합니다\", \"~해야 합니다\" 같은 표현을 사용하면 → 학습자는 손님/환자/고객 등\n" +
            "   - 그렇다면 당신은 상대방(종업원/직원/의사/택시 기사 등) 역할입니다\n\n" +
            "2. 당신은 상대방 역할이므로, 학습자(손님/환자/고객)에게 질문하세요\n" +
            "   - 손님이 할 만한 질문이 아닌, 당신(종업원/직원/의사 등)이 할 만한 질문을 생성하세요\n" +
            "   - 예: \"봉투 드릴까요?\" (O), \"봉투 주세요\" (X - 손님이 하는 말)\n\n" +
            "3. 상황에 맞는 자연스러운 질문을 생성하세요\n" +
            "   - 친절하고 자연스러운 말투 사용\n" +
            "   - 질문은 한 문장으로, 간단하고 명확하게\n" +
            "   - 인사말 없이 질문만 출력\n" +
            "   - 따옴표나 인용 부호 없이 순수하게 질문만 출력하세요\n" +
            "   - 답변이 간단한 단어나 짧은 문장으로 가능하도록\n\n" +
            "예시 분석 (반드시 이 패턴을 따르세요):\n" +
            "- \"편의점에 들러 필요한 물건을 사고 카운터에서 계산하려고 합니다\"\n" +
            "  → 학습자: 손님, 당신: 편의점 종업원\n" +
            "  → 올바른 질문: 봉투 드릴까요? / 더 필요한 물건 있으신가요? / 포인트 적립하시겠어요?\n" +
            "  → 잘못된 질문: 봉투 주세요 (X - 손님이 하는 말), 계산해주세요 (X - 손님이 하는 말)\n\n" +
            "- \"부천의 한 돈까스 집, 맘에드는 메뉴를 하나 주문하려고 합니다\"\n" +
            "  → 학습자: 손님, 당신: 식당 직원\n" +
            "  → 올바른 질문: 어떤 메뉴로 드릴까요? / 맵기는 어떻게 드릴까요? / 음료는 무엇으로 드릴까요?\n" +
            "  → 잘못된 질문: 돈까스 주세요 (X - 손님이 하는 말), 메뉴판 보여주세요 (X - 손님이 하는 말)\n\n" +
            "- \"목이 아파서 병원에 왔습니다, 의사 선생님께 정확한 증상을 설명해야 합니다\"\n" +
            "  → 학습자: 환자, 당신: 의사\n" +
            "  → 올바른 질문: 어디가 아프신가요? / 증상이 언제부터 시작되었나요? / 어떤 증상이 있으신가요?\n" +
            "  → 잘못된 질문: 약 처방해주세요 (X - 환자가 하는 말), 진단서 주세요 (X - 환자가 하는 말)\n\n" +
            "- \"택시 기사님께 정확한 목적지 주소를 말해야 합니다\"\n" +
            "  → 학습자: 승객, 당신: 택시 기사\n" +
            "  → 올바른 질문: 어디로 가시나요? / 목적지 주소 알려주세요 / 어느 쪽으로 가시나요?\n" +
            "  → 잘못된 질문: 택시 불러주세요 (X - 승객이 하는 말), 요금 얼마예요? (X - 승객이 하는 말)\n\n" +
            "중요: 출력할 때 따옴표나 인용 부호를 사용하지 말고, 순수하게 질문만 출력하세요.\n" +
            "예: \"더 필요한 물건 있으신가요?\" (X) → 더 필요한 물건 있으신가요? (O)",
            situationDescription
        );
        
        String userMessage = "위 상황 설명을 분석하여 상대방 역할의 첫 질문을 생성해주세요.";
        
        String response = callOpenAI(systemPrompt, userMessage);
        
        // 따옴표 제거 (응답이 따옴표로 감싸져 있을 경우)
        return removeQuotes(response);
    }
    
    @Override
    public EvaluationResult evaluateAnswer(String situationDescription, String question, String answer, int turnIndex) {
        String systemPrompt = String.format(
            "당신은 조음 장애 학습자의 말하기를 평가하는 전문가입니다.\n\n" +
            "상황 설명: %s\n" +
            "턴: %d번째\n\n" +
            "중요한 규칙:\n" +
            "1. 상황 설명을 분석하여 역할 관계를 파악하세요\n" +
            "   - 학습자는 손님/환자/고객 등\n" +
            "   - 질문하는 상대방은 종업원/직원/의사 등\n" +
            "   - 학습자가 상대방의 질문에 답변하는 형태입니다\n\n" +
            "평가 기준:\n" +
            "1. 단어 정확도 (70점): 발음 정확도를 중시합니다\n" +
            "   - 올바른 발음: 사과(O), 사구(X), 젓가락(O), 젇가락(X)\n" +
            "   - 자음/모음 정확도, 음절 정확도 평가\n\n" +
            "2. 맥락 적합성 (30점): 질문에 대한 답변이 적절한지 평가합니다\n" +
            "   - 질문과 답변의 관련성\n" +
            "   - 상황 설명에 맞는 답변인지\n" +
            "   - 역할 관계에 맞는 답변인지 (손님이 종업원 질문에 답하는 형태)\n\n" +
            "통과 기준: 50점 이상\n\n" +
            "피드백 작성 규칙:\n" +
            "- 한 문장으로 작성하세요\n" +
            "- 격려하는 톤을 유지하세요\n" +
            "- 성공 시: \"잘하셨어요!\" 같은 격려\n" +
            "- 실패 시: \"다시 한 번 시도해보세요\" 같은 격려\n\n" +
            "반드시 다음 JSON 형식으로만 응답하세요:\n" +
            "{\"isSuccess\": boolean, \"score\": 0-100, \"feedback\": \"한 문장\"}",
            situationDescription, turnIndex
        );
        
        String userMessage = String.format("질문: %s\n답변: %s", question, answer);
        
        String response = callOpenAI(systemPrompt, userMessage);
        
        try {
            Map<String, Object> evaluationMap = objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            boolean isSuccess = Boolean.TRUE.equals(evaluationMap.get("isSuccess"));
            float score = ((Number) evaluationMap.getOrDefault("score", 0)).floatValue();
            String feedback = (String) evaluationMap.getOrDefault("feedback", "평가를 완료했습니다.");
            
            return new EvaluationResult(isSuccess, score, feedback);
        } catch (Exception e) {
            log.error("OpenAI 평가 결과 파싱 실패", e);
            throw new OpenAIExceptions.OpenAIResponseParseException(e.getMessage());
        }
    }
    
    @Override
    public String generateNextQuestion(String situationDescription, List<Map<String, Object>> conversationHistory, int turnIndex) {
        String systemPrompt = String.format(
            "당신은 조음 장애 학습자를 위한 상황극 대화 상대입니다.\n\n" +
            "상황 설명: %s\n" +
            "현재 턴: %d번째 질문\n\n" +
            "중요한 규칙:\n" +
            "1. 상황 설명을 분석하여 학습자의 역할과 상대방의 역할을 파악하세요\n" +
            "   - 학습자가 행동하는 주체라면 → 당신은 상대방(종업원/직원/의사 등) 역할\n" +
            "   - 당신은 상대방 역할이므로, 학습자에게 질문하세요\n\n" +
            "2. 이전 대화 내용을 참고하여 자연스러운 대화 흐름을 유지하세요\n" +
            "   - 같은 주제를 반복하지 말고, 상황 내에서 다양한 주제로 질문\n" +
            "   - 질문은 한 문장으로, 간단하고 명확하게\n" +
            "   - 답변이 간단한 단어나 짧은 문장으로 가능하도록\n\n" +
            "3. 손님이 할 만한 질문이 아닌, 당신(상대방 역할)이 할 만한 질문을 생성하세요\n" +
            "4. 따옴표나 인용 부호 없이 순수하게 질문만 출력하세요\n\n" +
            "예시:\n" +
            "- 이전: 어떤 메뉴로 드릴까요? → 맵기는 어떻게 드릴까요? (O)\n" +
            "- 이전: 어떤 메뉴로 드릴까요? → 어떤 메뉴로 드릴까요? (X, 반복)\n" +
            "- 이전: 어떤 메뉴로 드릴까요? → 돈까스 주세요 (X, 손님이 하는 말)\n" +
            "- 이전: 어떤 메뉴로 드릴까요? → 오늘 날씨가 좋네요 (X, 상황과 무관)\n\n" +
            "중요: 출력할 때 따옴표나 인용 부호를 사용하지 말고, 순수하게 질문만 출력하세요.",
            situationDescription, turnIndex
        );
        
        String history = conversationHistory.stream()
            .map(turn -> {
                String question = (String) turn.get("question");
                String answer = (String) turn.get("answer");
                return (question != null ? "Q: " + question + " " : "") +
                       (answer != null ? "A: " + answer + " " : "");
            })
            .filter(s -> !s.isEmpty())
            .reduce("", (a, b) -> a + b)
            .trim();
        
        String userMessage = String.format(
            "이전 대화:\n%s\n\n위 대화를 참고하여 상대방 역할의 다음 질문을 생성해주세요.",
            history.isEmpty() ? "(없음)" : history
        );
        
        String response = callOpenAI(systemPrompt, userMessage);
        
        // 따옴표 제거 (응답이 따옴표로 감싸져 있을 경우)
        return removeQuotes(response);
    }
    
    @Override
    public String generateFinalFeedback(String situationDescription, float averageScore, boolean lastEvaluationSuccess) {
        String systemPrompt = String.format(
            "당신은 조음 장애 학습자를 격려하는 전문가입니다.\n\n" +
            "상황 설명: %s\n" +
            "평균 점수: %.1f점\n" +
            "마지막 평가: %s\n\n" +
            "다음 규칙을 따라 최종 피드백을 생성하세요:\n" +
            "1. 항상 격려하는 톤을 유지하세요\n" +
            "2. 한 문장으로 작성하세요\n" +
            "3. 점수에 관계없이 긍정적인 메시지를 전달하세요\n" +
            "4. 구체적인 칭찬이나 격려를 포함하세요\n\n" +
            "예시:\n" +
            "- 평균 80점, 성공: \"훌륭하게 완료하셨어요! 발음이 많이 좋아졌어요.\"\n" +
            "- 평균 60점, 성공: \"잘하셨어요! 계속 연습하면 더 좋아질 거예요.\"\n" +
            "- 평균 40점, 실패: \"노력하신 모습이 보여요. 조금만 더 연습하면 좋아질 거예요.\"\n" +
            "- 평균 30점, 실패: \"처음이 어려운 거예요. 포기하지 말고 계속 도전해보세요.\"",
            situationDescription, averageScore, lastEvaluationSuccess ? "성공" : "실패"
        );
        
        String userMessage = "위 정보를 바탕으로 학습자를 격려하는 최종 피드백을 생성해주세요.";
        
        String response = callOpenAI(systemPrompt, userMessage);
        
        // 따옴표 제거
        return removeQuotes(response);
    }
    
    /**
     * OpenAI API 호출
     */
    private String callOpenAI(String systemPrompt, String userMessage) {
        try {
            String url = openAIProperties.getBaseUrl() + CHAT_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAIProperties.getKey());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAIProperties.getModel());
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.5);  // 일관성과 창의성의 균형
            requestBody.put("max_tokens", 200);   // 충분한 길이 확보
            requestBody.put("top_p", 0.9);        // 다양성 제어
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ParameterizedTypeReference<Map<String, Object>> responseType = 
                new ParameterizedTypeReference<Map<String, Object>>() {};
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, responseType
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    return content != null ? content.trim() : "";
                }
            }
            
            log.warn("OpenAI API 응답에 content가 없습니다: {}", responseBody);
            throw new OpenAIExceptions.OpenAIEmptyResponseException();
            
        } catch (OpenAIExceptions.OpenAIEmptyResponseException | OpenAIExceptions.OpenAIResponseParseException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new OpenAIExceptions.OpenAIServiceException(e.getMessage(), e);
        }
    }
    
    /**
     * 응답에서 앞뒤 따옴표 제거
     */
    private String removeQuotes(String response) {
        if (response == null || response.isBlank()) {
            return "";
        }
        
        String cleaned = response.trim();
        
        // 앞뒤 따옴표 제거 (큰따옴표, 작은따옴표)
        while ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
               (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        
        return cleaned;
    }
}

