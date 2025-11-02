package com.example.GoSonGim_BE.domain.situation.repository;

import com.example.GoSonGim_BE.domain.situation.entity.Situation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SituationRepository extends JpaRepository<Situation, Long> {
    List<Situation> findByIdIn(List<Long> ids);
}
