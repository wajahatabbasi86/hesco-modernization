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

    /** Auto-generated as 'T-' + the form's GPS number (SRS §8.3.5) — computed in SurveyService, not client-supplied. */
    @Column(name = "equipment_number", length = 40)
    private String equipmentNumber;

    /** Snapshot summary of phases captured against this survey_form's conductor_detail rows, e.g. "R,Y,B". */
    @Column(name = "equipment_phase", length = 10)
    private String equipmentPhase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_use_id")
    private ItemType equipmentUse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mounting_id")
    private ItemType mounting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fuses_id")
    private ItemType fuses;

    @Column(name = "asset_code", length = 50)
    private String assetCode;

    @Column(name = "consumer_name", length = 150)
    private String consumerName;

    @Column(name = "equipment_location", length = 255)
    private String equipmentLocation;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}