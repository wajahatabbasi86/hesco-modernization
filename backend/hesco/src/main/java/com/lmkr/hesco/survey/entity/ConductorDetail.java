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
  * Conductor type for the line segment a survey_form's
  * line_length_meters describes (SRS §8.3.4). conductorType should
  * belong to the HT_CONDUCTOR or LT_CONDUCTOR item_category depending on
  * the work order's survey type; enforced in the service layer.
  */
        @Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conductor_detail")
public class ConductorDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_form_id", nullable = false, unique = true)
    private SurveyForm surveyForm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conductor_type_id", nullable = false)
    private ItemType conductorType;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}