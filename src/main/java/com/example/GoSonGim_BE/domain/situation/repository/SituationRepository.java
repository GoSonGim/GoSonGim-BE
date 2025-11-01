package com.example.GoSonGim_BE.domain.situation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.GoSonGim_BE.domain.situation.entity.Situation;

@Repository
public interface SituationRepository extends JpaRepository<Situation, Long> {
}
