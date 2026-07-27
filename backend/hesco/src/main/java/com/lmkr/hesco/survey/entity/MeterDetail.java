package com.lmkr.hesco.survey.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
  * Meter attributes for a survey_form whose equipment type is METER
  * (SRS §8.3.6). Kept minimal (meterNumber, consumerReference) pending
  * a full §8.3.6 field-list review — extend this entity rather than
  * widening SurveyForm once that's confirmed.
  */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "meter_detail")
public class MeterDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_form_id", nullable = false, unique = true)
    private SurveyForm surveyForm;

    @Column(name = "meter_number", length = 30)
    private String meterNumber;

    @Column(name = "consumer_reference", length = 50)
    private String consumerReference;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
