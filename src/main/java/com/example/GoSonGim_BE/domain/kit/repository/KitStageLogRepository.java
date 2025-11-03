package com.example.GoSonGim_BE.domain.kit.repository;

import com.example.GoSonGim_BE.domain.kit.entity.KitStageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KitStageLogRepository extends JpaRepository<KitStageLog, Long> {
    
    /**
     * 사용자가 성공한 고유 Kit 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 성공한 고유 Kit 개수
     */
    @Query("SELECT COUNT(DISTINCT ks.kit.id) FROM KitStageLog ksl " +
           "JOIN ksl.kitStage ks " +
           "WHERE ksl.user.id = :userId AND ksl.isSuccess = true")
    Long countDistinctSuccessfulKitsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자가 학습한 고유 단어 개수 조회 (성공한 것만)
     * 
     * @param userId 사용자 ID
     * @return 학습한 고유 단어 개수
     */
    @Query("SELECT COUNT(DISTINCT ksl.targetWord) FROM KitStageLog ksl " +
           "WHERE ksl.user.id = :userId AND ksl.isSuccess = true " +
           "AND ksl.targetWord IS NOT NULL")
    Long countDistinctSuccessfulWordsByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자가 모든 단계를 완료한 Kit ID 조회
     * 
     * @param userId 사용자 ID
     * @return 완료한 Kit ID 목록
     */
    @Query("SELECT ks.kit.id FROM KitStageLog ksl " +
           "JOIN ksl.kitStage ks " +
           "WHERE ksl.user.id = :userId AND ksl.isSuccess = true " +
           "GROUP BY ks.kit.id " +
           "HAVING COUNT(DISTINCT ks.id) = " +
           "(SELECT COUNT(ks2.id) FROM KitStage ks2 WHERE ks2.kit.id = ks.kit.id)")
    List<Long> findCompletedKitIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 최근 5일간 일별 성공한 학습 개수 조회 (중복 허용)
     * 
     * @param userId 사용자 ID
     * @param startDate 시작일 (5일 전)
     * @param endDate 종료일 (오늘)
     * @return 일자별 성공 개수 리스트
     */
    @Query("SELECT DATE(ksl.createdAt) as date, COUNT(ksl.id) as count " +
           "FROM KitStageLog ksl " +
           "WHERE ksl.user.id = :userId " +
           "AND ksl.isSuccess = true " +
           "AND DATE(ksl.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(ksl.createdAt) " +
           "ORDER BY DATE(ksl.createdAt)")
    List<Object[]> countDailySuccessfulLearning(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    
    /**
     * 사용자의 총 성공 횟수 조회
     * 
     * @param userId 사용자 ID
     * @return 총 성공 횟수
     */
    @Query("SELECT COUNT(ksl.id) FROM KitStageLog ksl " +
           "WHERE ksl.user.id = :userId AND ksl.isSuccess = true")
    Long countTotalSuccessfulByUserId(@Param("userId") Long userId);
    
    /**
     * 날짜별 학습한 단어 목록 조회 (페이지네이션)
     * 
     * @param userId 사용자 ID
     * @param pageable 페이지 정보
     * @return 날짜별 그룹핑된 결과
     */
    @Query("SELECT DATE(ksl.createdAt) as date, " +
           "COUNT(DISTINCT ksl.targetWord) as wordCount, " +
           "GROUP_CONCAT(DISTINCT ksl.targetWord) as words " +
           "FROM KitStageLog ksl " +
           "WHERE ksl.user.id = :userId " +
           "AND ksl.isSuccess = true " +
           "AND ksl.targetWord IS NOT NULL " +
           "GROUP BY DATE(ksl.createdAt) " +
           "ORDER BY DATE(ksl.createdAt) DESC")
    List<Object[]> findDailyWordsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 사용자의 총 학습 날짜 수 조회 (페이지네이션용)
     * 
     * @param userId 사용자 ID
     * @return 총 학습 날짜 수
     */
    @Query("SELECT COUNT(DISTINCT DATE(ksl.createdAt)) " +
           "FROM KitStageLog ksl " +
           "WHERE ksl.user.id = :userId " +
           "AND ksl.isSuccess = true " +
           "AND ksl.targetWord IS NOT NULL")
    Long countDistinctDaysByUserId(@Param("userId") Long userId);
}