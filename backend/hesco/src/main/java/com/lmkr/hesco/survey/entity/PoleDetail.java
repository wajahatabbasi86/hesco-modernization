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
  * Pole attributes for a survey_form whose equipment type is
  * FEEDER_POLE, PRIMARY_POLE, or SECONDARY_POLE (SRS §8.3.3). structureType
  * should belong to the PRIMARY_STRUCTURE or SECONDARY_STRUCTURE
  * item_category depending on which; that constraint is enforced in the
  * service layer, not the DB, same as EquipmentSequenceValidator.
  */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pole_detail")
public class PoleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_form_id", nullable = false, unique = true)
    private SurveyForm surveyForm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "structure_type_id", nullable = false)
    private ItemType structureType;

    @Column(name = "pole_number", length = 30)
    private String poleNumber;

    @Column(name = "height_meters", precision = 6, scale = 2)
    private java.math.BigDecimal heightMeters;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
