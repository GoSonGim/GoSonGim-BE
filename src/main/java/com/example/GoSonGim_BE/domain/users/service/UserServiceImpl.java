package com.example.GoSonGim_BE.domain.users.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final KitStageLogRepository kitStageLogRepository;
    private final SituationLogRepository situationLogRepository;
    
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
            LocalDate date = (LocalDate) result[0];
            Long count = (Long) result[1];
            dailyCountMap.put(date, count.intValue());
        }
        
        // 5일간의 데이터 리스트 생성 (없는 날은 0으로)
        List<Integer> kitRecentDayCounts = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            kitRecentDayCounts.add(dailyCountMap.getOrDefault(date, 0));
        }
        
        return UserStudyStatisticsResponse.of(wordCount, situationCount, kitCount, 
                                              kitTotalSuccessCount, kitRecentDayCounts);
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
        
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
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
        }
        
        // 다음 페이지 존재 여부 확인
        Long totalDays = kitStageLogRepository.countDistinctDaysByUserId(userId);
        boolean hasNext = (long) (pageNumber + 1) * pageSize < totalDays;
        
        return DailyWordsResponse.of(items, page, pageSize, hasNext);
    }
}
