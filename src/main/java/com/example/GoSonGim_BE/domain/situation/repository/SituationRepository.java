package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.Situation;
import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SituationRepository extends JpaRepository<Situation, Long> {
    List<Situation> findByIdIn(List<Long> ids);
    
    /**
     * 카테고리별 상황극 목록 조회
     */
    List<Situation> findBySituationCategory(SituationCategory category);
    
    /**
     * 전체 상황극 목록 조회
     */
    List<Situation> findAll();
}
