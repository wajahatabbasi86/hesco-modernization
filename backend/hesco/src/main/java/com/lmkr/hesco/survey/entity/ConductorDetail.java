package com.lmkr.hesco.survey.entity;

import com.lmkr.hesco.warehouse.entity.ItemType;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Conductor type for ONE PHASE of the line segment a survey_form's
 * line_length_meters describes (SRS §8.3.4). One survey_form (an End
 * Point) has one ConductorDetail row PER active phase — R/Y/B always,
 * plus N when the resolved conductor category is LT_CONDUCTOR. The
 * client's "All Phases" shortcut just means the same conductorTypeCode
 * gets sent for every active phase — SurveyService still writes one row
 * per phase, so this is not a special case at the persistence layer.
 * conductorType should belong to the HT_CONDUCTOR or LT_CONDUCTOR
 * item_category depending on the work order's survey type; enforced in
 * the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conductor_detail",
        uniqueConstraints = @UniqueConstraint(columnNames = {"survey_form_id", "phase"}))
public class ConductorDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_form_id", nullable = false)
    private SurveyForm surveyForm;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "phase", nullable = false)
    private ConductorPhase phase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conductor_type_id", nullable = false)
    private ItemType conductorType;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
