package com.lmkr.hesco.survey.entity;

import com.lmkr.hesco.warehouse.entity.ItemType;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
  * Transformer attributes for a survey_form whose equipment type is
  * TRANSFORMER (SRS §8.3.5). capacity should belong to the
  * TRANSFORMER_CAPACITY item_category. cableSize/ctRatio mirror
  * power_transformer's grid-station-level columns — this is the same
  * attribute set captured at survey time.
  */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transformer_detail")
public class TransformerDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_form_id", nullable = false, unique = true)
    private SurveyForm surveyForm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capacity_id", nullable = false)
    private ItemType capacity;

    @Column(name = "transformer_name", length = 100)
    private String transformerName;

    @Column(name = "cable_size", length = 30)
    private String cableSize;

    @Column(name = "ct_ratio", length = 30)
    private String ctRatio;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}