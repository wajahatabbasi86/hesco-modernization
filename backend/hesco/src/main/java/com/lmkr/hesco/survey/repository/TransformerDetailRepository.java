package com.lmkr.hesco.survey.repository;

import com.lmkr.hesco.survey.entity.TransformerDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransformerDetailRepository extends JpaRepository<TransformerDetail, Long> {
    Optional<TransformerDetail> findBySurveyFormId(Long surveyFormId);
}