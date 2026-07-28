package com.lmkr.hesco.survey.repository;

import com.lmkr.hesco.survey.entity.ConductorDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConductorDetailRepository extends JpaRepository<ConductorDetail, Long> {
    Optional<ConductorDetail> findBySurveyFormId(Long surveyFormId);
}