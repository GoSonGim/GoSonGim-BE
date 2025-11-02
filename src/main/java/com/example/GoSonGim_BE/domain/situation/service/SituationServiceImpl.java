package com.example.GoSonGim_BE.domain.situation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GoSonGim_BE.domain.situation.dto.request.SituationCreateRequest;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationCreateResponse;
import com.example.GoSonGim_BE.domain.situation.dto.response.SituationListResponse;
import com.example.GoSonGim_BE.domain.situation.entity.Situation;
import com.example.GoSonGim_BE.domain.situation.entity.SituationCategory;
import com.example.GoSonGim_BE.domain.situation.repository.SituationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SituationServiceImpl implements SituationService {

    private final SituationRepository situationRepository;

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
        
        return SituationCreateResponse.from(savedSituation);
    }

    @Override
    public SituationListResponse getSituationsByCategory(String category) {
        log.debug("Received category parameter: {}", category);
        SituationCategory situationCategory = SituationCategory.from(category);
        log.debug("Converted to enum: {}", situationCategory);
        
        List<Situation> situations;
        if (situationCategory.isAll()) {
            log.debug("Fetching all situations");
            situations = situationRepository.findAll();
        } else {
            log.debug("Fetching situations for category: {}", situationCategory);
            situations = situationRepository.findBySituationCategory(situationCategory);
            log.debug("Found {} situations", situations.size());
        }
        
        return SituationListResponse.from(situations);
    }
}
