package com.lmkr.hesco.survey.repository;

import com.lmkr.hesco.survey.entity.SurveyForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyFormRepository extends JpaRepository<SurveyForm, Long> {
    Optional<SurveyForm> findByGpsNumber(String gpsNumber);
    List<SurveyForm> findByWorkOrderIdOrderByIdAsc(Long workOrderId);
}
