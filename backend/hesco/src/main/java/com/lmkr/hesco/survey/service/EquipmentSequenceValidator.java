package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.entity.EquipmentType;
import com.lmkr.hesco.survey.entity.SePointType;
import com.lmkr.hesco.survey.exception.InvalidEquipmentSequenceException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Owns equipment Start/End Point legality (SRS §8.3.2). Reads
 * equipment_sequence_transition (plain reference data, 03_reference_data_
 * and_equipment_sequence.sql) but the enforcement itself — is this a legal
 * Start Point given the previous form's End Point equipment, is this
 * equipment type even allowed to be a Start/End at all — is Java, not a
 * DB trigger.
 *
 * survey-mobile-app ships a cached copy of the transition data for
 * offline enforcement; survey-service re-validates on sync using this
 * same class server-side, so there is exactly one implementation of the
 * rule, just deployed twice (Kotlin/Android port + this Java version).
 */
@Component
public class EquipmentSequenceValidator {

    private final EquipmentSequenceTransitionRepository transitionRepository;

    public EquipmentSequenceValidator(EquipmentSequenceTransitionRepository transitionRepository) {
        this.transitionRepository = transitionRepository;
    }

    /**
     * @param sePoint               which S/E point this form represents
     * @param equipmentType         the equipment type chosen/auto-filled for this form
     * @param previousFormEndEquipment the End Point equipment type of the immediately
     *                               preceding form in this work order, or empty if this
     *                               is the first form (which must be a Start Point /
     *                               Feeder Pole per SRS §8.3.1)
     */
    public void validate(SePointType sePoint, EquipmentType equipmentType,
                          Optional<EquipmentType> previousFormEndEquipment) {

        if (sePoint == SePointType.START_POINT && !equipmentType.isCanBeStart()) {
            throw new InvalidEquipmentSequenceException(
                equipmentType.getCode() + " cannot be a Start Point (SRS §8.3.2)");
        }
        if (sePoint == SePointType.END_POINT && !equipmentType.isCanBeEnd()) {
            throw new InvalidEquipmentSequenceException(
                equipmentType.getCode() + " cannot be an End Point (SRS §8.3.2)");
        }

        if (previousFormEndEquipment.isEmpty()) {
            // First form in the work order: must start at Feeder Pole.
            if (sePoint == SePointType.START_POINT && !equipmentType.getCode().equals("FEEDER_POLE")) {
                throw new InvalidEquipmentSequenceException(
                    "The first form in a work order must start at Feeder Pole (SRS §8.3.1)");
            }
            return;
        }

        if (sePoint != SePointType.START_POINT) {
            return; // continuity rule only applies to a form's Start Point
        }

        EquipmentType prevEnd = previousFormEndEquipment.get();
        Set<String> legalNextStarts = transitionRepository.legalNextStartCodes(prevEnd.getId());
        if (!legalNextStarts.contains(equipmentType.getCode())) {
            throw new InvalidEquipmentSequenceException(String.format(
                "Start Point %s is not a legal continuation after End Point %s (SRS §8.3.2)",
                equipmentType.getCode(), prevEnd.getCode()));
        }
    }
}
