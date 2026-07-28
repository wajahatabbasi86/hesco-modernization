package com.lmkr.hesco.survey.entity;

import java.math.BigDecimal;
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
  * (SRS §8.3.6). Also carries sanctionedLoad/meterMake, added for the
  * Meter Report (SRS §3.15.2.4) — those two aren't otherwise captured
  * on the mobile form yet; extend this entity rather than
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

    @Column(name = "sanctioned_load", precision = 10, scale = 2)
    private BigDecimal sanctionedLoad;

    @Column(name = "meter_make", length = 100)
    private String meterMake;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
