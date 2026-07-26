package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.entity.EquipmentSequenceTransition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Set;

public interface EquipmentSequenceTransitionRepository extends Repository<EquipmentSequenceTransition, Integer> {

    @Query("select t.toStartEquipment.code from EquipmentSequenceTransition t " +
           "where t.fromEndEquipment.id = :fromEndEquipmentId")
    Set<String> legalNextStartCodes(Integer fromEndEquipmentId);
}
