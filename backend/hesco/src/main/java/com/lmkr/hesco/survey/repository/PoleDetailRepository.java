package com.lmkr.hesco.survey.repository;

import com.lmkr.hesco.survey.entity.PoleDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoleDetailRepository extends JpaRepository<PoleDetail, Long> {
    Optional<PoleDetail> findBySurveyFormId(Long surveyFormId);
}
