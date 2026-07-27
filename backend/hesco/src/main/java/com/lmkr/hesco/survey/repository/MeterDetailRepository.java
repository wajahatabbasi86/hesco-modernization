package com.lmkr.hesco.survey.repository;

import com.lmkr.hesco.survey.entity.MeterDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterDetailRepository extends JpaRepository<MeterDetail, Long> {
    Optional<MeterDetail> findBySurveyFormId(Long surveyFormId);
}