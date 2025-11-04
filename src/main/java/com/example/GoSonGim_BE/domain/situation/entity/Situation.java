package com.example.GoSonGim_BE.domain.situation.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Comment;

import com.example.GoSonGim_BE.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상황극
 */
@Entity
@Table(name = "situation", indexes = {
    @Index(name = "idx_situation_category", columnList = "situation_category")
})
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Situation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Comment("카테고리")
    @Enumerated(EnumType.STRING)
    @Column(name = "situation_category", nullable = false, length = 20)
    private SituationCategory situationCategory;

    @Comment("상황극명")
    @Column(name = "situation_name", nullable = false, length = 100)
    private String situationName;

    @Comment("설명")
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Comment("이미지 URL")
    @Column(name = "image")
    private String image;

    @OneToMany(mappedBy = "situation", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<SituationLog> situationLogs = new ArrayList<>();

    @Builder
    public Situation(SituationCategory situationCategory, String situationName, 
                     String description, String image) {
        this.situationCategory = situationCategory;
        this.situationName = situationName;
        this.description = description;
        this.image = image;
        this.situationLogs = new ArrayList<>();
    }
}