package com.example.GoSonGim_BE.domain.kit.service;

import com.example.GoSonGim_BE.domain.kit.repository.KitRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageRepository;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KitServiceImpl implements KitService {
    
    private final KitRepository kitRepository;
    private final KitStageRepository kitStageRepository;
    private final KitStageLogRepository kitStageLogRepository;
    private final UserRepository userRepository;
    
    @Override
    public KitStagesResponse getKitStages(Long userId, Long kitId) {
        //User user = userRepository.findById(userId).orElseThrow(KitExceptions.UserNotFound::new);
        //Kit kit = kitRepository.findById(kitId).orElseThrow(KitExceptions.KitNotFound::new);

    }
    
    @Override
    @Transactional
    public EvaluateResponse evaluatePronunciation(Long userId, EvaluateRequest request) {

    }
    
    @Override
    @Transactional
    public StageLogResponse saveStageLog(Long userId, StageLogRequest request) {

    }
    
    @Override
    @Transactional
    public DiagnosisResponse performDiagnosis(Long userId, DiagnosisRequest request) {

    }
}