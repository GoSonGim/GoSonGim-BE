package com.example.GoSonGim_BE.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.auth.repository.RefreshTokenRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.situation.repository.SituationLogRepository;
import com.example.GoSonGim_BE.domain.users.dto.response.DailyWordsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.NicknameChangeResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserProfileResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserStudyStatisticsResponse;
import com.example.GoSonGim_BE.domain.users.dto.response.UserWithdrawalResponse;
import com.example.GoSonGim_BE.domain.users.entity.User;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;
import com.example.GoSonGim_BE.domain.users.exception.UserExceptions;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final KitStageLogRepository kitStageLogRepository;
    private final SituationLogRepository situationLogRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Override
    public User createDefaultUser() {
        User user = User.createDefault();
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        return UserProfileResponse.from(
            user.getId(),
            user.getNickname(),
            user.getLevel().getDescription()
        );
    }
    
    @Override
    public UserWithdrawalResponse withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        // 리프레시 토큰 삭제
        refreshTokenRepository.deleteByUserId(userId);
        
        // 사용자 소프트 삭제
        user.delete();
        userRepository.save(user);
        
        return UserWithdrawalResponse.of(user.getDeletedAt());
    }
    
    @Override
    public NicknameChangeResponse changeNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        user.updateNickname(nickname);
        userRepository.save(user);
        
        return NicknameChangeResponse.of(user.getNickname());
    }
    
    /**
     * 사용자 레벨 업데이트
     * 성공한 고유 키트 수를 기반으로 레벨 재계산
     */
    public void updateUserLevel(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        Long uniqueSuccessfulKits = kitStageLogRepository.countDistinctSuccessfulKitsByUserId(userId);
        user.calculateAndUpdateLevel(uniqueSuccessfulKits);
        userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserStudyStatisticsResponse getUserStudyStatistics(Long userId) {
        // 사용자 존재 확인
        userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        // 학습한 고유 단어 수 (중복 제거)
        Long wordCount = kitStageLogRepository.countDistinctSuccessfulWordsByUserId(userId);
        
        // 학습한 상황 수
        Long situationCount = situationLogRepository.countDistinctSuccessfulSituationsByUserId(userId);
        
        // 모든 단계를 완료한 키트 수
        Long kitCount = (long) kitStageLogRepository.findCompletedKitIdsByUserId(userId).size();
        
        // Kit 그래프 데이터 - 총 성공 횟수
        Long kitTotalSuccessCount = kitStageLogRepository.countTotalSuccessfulByUserId(userId);
        
        // 최근 5일간 일별 데이터 조회
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.minusDays(4);
        
        List<Object[]> dailyResults = kitStageLogRepository.countDailySuccessfulLearning(userId, fiveDaysAgo, today);
        
        // 날짜별 카운트를 Map으로 변환
        Map<LocalDate, Integer> dailyCountMap = new HashMap<>();
        for (Object[] result : dailyResults) {
            java.sql.Date sqlDate = (java.sql.Date) result[0];
            LocalDate date = sqlDate.toLocalDate();
            Long count = (Long) result[1];
            dailyCountMap.put(date, count.intValue());
        }
        
        // 5일간의 데이터 리스트 생성 (없는 날은 0으로)
        List<Integer> kitRecentDayCounts = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            kitRecentDayCounts.add(dailyCountMap.getOrDefault(date, 0));
        }

        // Situation 그래프 데이터 - 총 성공 횟수 (전체 기간)
        List<String> allSituationConversations = situationLogRepository.findAllSuccessfulConversations(userId);
        ObjectMapper objectMapper = new ObjectMapper();
        Long situationTotalSuccessCount = 0L;

        for (String conversationJson : allSituationConversations) {
            situationTotalSuccessCount += parseMaxTurnIndex(conversationJson, objectMapper);
        }

        // Situation 최근 5일간 일별 데이터 조회
        List<Object[]> situationDailyResults = situationLogRepository.findSuccessfulLogsWithConversation(userId, fiveDaysAgo, today);

        // 날짜별 카운트를 Map으로 변환
        Map<LocalDate, Integer> situationDailyCountMap = new HashMap<>();
        for (Object[] result : situationDailyResults) {
            java.sql.Date sqlDate = (java.sql.Date) result[0];
            LocalDate date = sqlDate.toLocalDate();
            String conversationJson = (String) result[1];

            int maxTurnIndex = parseMaxTurnIndex(conversationJson, objectMapper);
            situationDailyCountMap.merge(date, maxTurnIndex, Integer::sum);
        }

        // 5일간의 데이터 리스트 생성 (없는 날은 0으로)
        List<Integer> situationRecentDayCounts = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            situationRecentDayCounts.add(situationDailyCountMap.getOrDefault(date, 0));
        }

        return UserStudyStatisticsResponse.of(wordCount, situationCount, kitCount,
                                              kitTotalSuccessCount, kitRecentDayCounts,
                                              situationTotalSuccessCount, situationRecentDayCounts);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DailyWordsResponse getDailyWords(Long userId, int page, int size) {
        // 사용자 존재 확인
        userRepository.findById(userId)
            .orElseThrow(() -> new UserExceptions.UserNotFoundException(userId));
        
        // 페이지 번호는 0부터 시작 (사용자는 1부터 입력)
        int pageNumber = Math.max(0, page - 1);
        
        // 사이즈 제한 (최대 100)
        int pageSize = Math.min(Math.max(1, size), 100);
        
        // 페이지 요청 생성
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        
        // 날짜별 단어 목록 조회
        List<Object[]> results = kitStageLogRepository.findDailyWordsByUserId(userId, pageable);
        
        // 결과를 DTO로 변환
        List<DailyWordsResponse.DailyWordItem> items = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.M.d");
        int totalWordCount = 0;
        
        for (Object[] result : results) {
            java.sql.Date sqlDate = (java.sql.Date) result[0];
            LocalDate date = sqlDate.toLocalDate();
            Long wordCount = (Long) result[1];
            String wordsString = (String) result[2];
            
            // 단어 문자열을 리스트로 변환
            List<String> words = wordsString != null ? 
                Arrays.asList(wordsString.split(",")) : 
                new ArrayList<>();
            
            items.add(new DailyWordsResponse.DailyWordItem(
                date.format(formatter),
                wordCount.intValue(),
                words
            ));
            
            // 총 단어 수 누적
            totalWordCount += wordCount.intValue();
        }
        
        // 다음 페이지 존재 여부 확인
        Long totalDays = kitStageLogRepository.countDistinctDaysByUserId(userId);
        boolean hasNext = (long) (pageNumber + 1) * pageSize < totalDays;
        
        return DailyWordsResponse.of(items, totalWordCount, page, pageSize, hasNext);
    }

    /**
     * conversation JSON에서 turnIndex의 최대값을 추출
     * 
     * @param conversationJson conversation JSON 문자열
     * @param objectMapper JSON 파서
     * @return turnIndex의 최대값
     */
    private int parseMaxTurnIndex(String conversationJson, ObjectMapper objectMapper) {
        if (conversationJson == null || conversationJson.isBlank()) {
            return 0;
        }
        
        try {
            JsonNode conversations = objectMapper.readTree(conversationJson);
            int maxTurnIndex = 0;
            
            for (JsonNode conv : conversations) {
                if (conv.has("turnIndex")) {
                    int turnIndex = conv.get("turnIndex").asInt();
                    maxTurnIndex = Math.max(maxTurnIndex, turnIndex);
                }
            }
            
            return maxTurnIndex;
        } catch (Exception e) {
            // JSON 파싱 실패 시 0 반환
            return 0;
        }
    }
}
