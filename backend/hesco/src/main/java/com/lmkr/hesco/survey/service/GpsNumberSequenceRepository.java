package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.entity.SurveyForm;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface GpsNumberSequenceRepository extends Repository<SurveyForm, Long> {

    @Query("select case when count(s) > 0 then true else false end from SurveyForm s " +
           "where s.gpsNumber = :gpsNumber")
    boolean existsByGpsNumber(String gpsNumber);
}
