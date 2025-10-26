package com.example.GoSonGim_BE.domain.kit_stage.entity;

import com.example.GoSonGim_BE.domain.kit.entity.Kit;
import com.example.GoSonGim_BE.domain.kit_stage_log.entity.KitStageLog;
import com.example.GoSonGim_BE.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * 키트 단계
 */
@Entity
@Table(name = "kit_stage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KitStage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("키트 참조")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id", nullable = false)
    private Kit kit;

    @Comment("키트 단계명")
    @Column(name = "kit_stage_name", nullable = false)
    private String kitStageName;

    @OneToMany(mappedBy = "kitStage", cascade = CascadeType.ALL)
    @Builder.Default
    private List<KitStageLog> kitStageLogs = new ArrayList<>();
}