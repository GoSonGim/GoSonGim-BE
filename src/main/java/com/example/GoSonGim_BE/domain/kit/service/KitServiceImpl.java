package com.example.GoSonGim_BE.domain.kit.service;

import com.example.GoSonGim_BE.domain.kit.dto.response.KitCategoriesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitStagesResponse;
import com.example.GoSonGim_BE.domain.kit.dto.response.KitsResponse;
import com.example.GoSonGim_BE.domain.kit.entity.Kit;
import com.example.GoSonGim_BE.domain.kit.entity.KitCategory;
import com.example.GoSonGim_BE.domain.kit.entity.KitStage;
import com.example.GoSonGim_BE.domain.kit.repository.KitCategoryRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageLogRepository;
import com.example.GoSonGim_BE.domain.kit.repository.KitStageRepository;
import com.example.GoSonGim_BE.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KitServiceImpl implements KitService {
    
    private final KitRepository kitRepository;
    private final KitCategoryRepository kitCategoryRepository;
    private final KitStageRepository kitStageRepository;
    private final KitStageLogRepository kitStageLogRepository;
    private final UserRepository userRepository;

    @Override
    public KitCategoriesResponse getKitCategories() {
        List<KitCategory> categories = kitCategoryRepository.findAll();
        
        List<KitCategoriesResponse.Category> categoryDtos = categories.stream()
            .map(category -> new KitCategoriesResponse.Category(
                category.getId(),
                category.getKitCategoryName()
            ))
            .collect(Collectors.toList());
        
        return new KitCategoriesResponse(categoryDtos.size(), categoryDtos);
    }

    @Override
    public KitsResponse getKitsByCategoryId(Long categoryId) {
        List<Kit> kits = kitRepository.findByKitCategoryId(categoryId);
        
        List<KitsResponse.Kit> kitDtos = kits.stream()
            .map(kit -> new KitsResponse.Kit(
                kit.getId(),
                kit.getKitName()
            ))
            .collect(Collectors.toList());
        
        return new KitsResponse(kitDtos.size(), kitDtos);
    }

    @Override
    public KitStagesResponse getKitStages(Long kitId) {
        Kit kit = kitRepository.findById(kitId)
            .orElseThrow(() -> new RuntimeException("키트를 찾을 수 없습니다."));
        
        List<KitStage> stages = kit.getKitStages();
        
        List<KitStagesResponse.Stage> stageDtos = stages.stream()
            .map(stage -> new KitStagesResponse.Stage(
                stage.getId(),
                stage.getKitStageName()
            ))
            .collect(Collectors.toList());
        
        return new KitStagesResponse(
            kit.getId(),
            kit.getKitName(),
            kit.getKitCategory().getKitCategoryName(),
            stageDtos.size(),
            stageDtos
        );
    }

}