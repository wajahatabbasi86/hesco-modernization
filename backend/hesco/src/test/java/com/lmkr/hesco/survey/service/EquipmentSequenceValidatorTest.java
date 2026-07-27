package com.lmkr.hesco.survey.service;

import com.lmkr.hesco.survey.entity.EquipmentType;
import com.lmkr.hesco.survey.entity.SePointType;
import com.lmkr.hesco.survey.exception.InvalidEquipmentSequenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * SRS §8.3.2 Equipment Type Sequence & Rules. Feeder Pole -> Primary Pole
 * -> Transformer -> Secondary Pole -> Meter, with Start/End point
 * legality per equipment type. equipment_sequence_transition is plain
 * reference data (no DB trigger sits behind it); this class is the sole
 * enforcement point, so it's mocked out here rather than needing a live
 * Postgres instance loaded with seed data.
 */
@ExtendWith(MockitoExtension.class)
class EquipmentSequenceValidatorTest {

    @Mock
    private EquipmentSequenceTransitionRepository transitionRepository;

    private EquipmentSequenceValidator validator;

    private EquipmentType feederPole;
    private EquipmentType primaryPole;
    private EquipmentType transformer;
    private EquipmentType secondaryPole;
    private EquipmentType meter;

    @BeforeEach
    void setUp() {
        validator = new EquipmentSequenceValidator(transitionRepository);

        feederPole = equipmentType(1, "FEEDER_POLE", true, false);
        primaryPole = equipmentType(2, "PRIMARY_POLE", true, true);
        transformer = equipmentType(3, "TRANSFORMER", false, true);
        secondaryPole = equipmentType(4, "SECONDARY_POLE", true, false);
        meter = equipmentType(5, "METER", false, true);
    }

    private EquipmentType equipmentType(int id, String code, boolean canBeStart, boolean canBeEnd) {
        EquipmentType type = new EquipmentType();
        type.setId(id);
        type.setCode(code);
        type.setCanBeStart(canBeStart);
        type.setCanBeEnd(canBeEnd);
        return type;
    }

    @Test
    void firstFormInWorkOrder_startingAtFeederPole_isAccepted() {
        validator.validate(SePointType.START_POINT, feederPole, Optional.empty());
    }

    @Test
    void firstFormInWorkOrder_notStartingAtFeederPole_throws() {
        assertThatThrownBy(() ->
            validator.validate(SePointType.START_POINT, primaryPole, Optional.empty()))
            .isInstanceOf(InvalidEquipmentSequenceException.class)
            .hasMessageContaining("must start at Feeder Pole");
    }

    @Test
    void transformer_asStartPoint_throws() {
        // Transformer can never be a Start Point (SRS §8.3.2)
        assertThatThrownBy(() ->
            validator.validate(SePointType.START_POINT, transformer, Optional.of(primaryPole)))
            .isInstanceOf(InvalidEquipmentSequenceException.class)
            .hasMessageContaining("cannot be a Start Point");
    }

    @Test
    void secondaryPole_asEndPoint_throws() {
        // Secondary Pole can never be an End Point (SRS §8.3.2)
        assertThatThrownBy(() ->
            validator.validate(SePointType.END_POINT, secondaryPole, Optional.of(primaryPole)))
            .isInstanceOf(InvalidEquipmentSequenceException.class)
            .hasMessageContaining("cannot be an End Point");
    }

    @Test
    void startPoint_legalContinuationAfterPreviousEnd_isAccepted() {
        when(transitionRepository.legalNextStartCodes(primaryPole.getId()))
            .thenReturn(Set.of("PRIMARY_POLE", "SECONDARY_POLE"));

        validator.validate(SePointType.START_POINT, secondaryPole, Optional.of(primaryPole));
    }

    @Test
    void startPoint_illegalContinuationAfterPreviousEnd_throws() {
        when(transitionRepository.legalNextStartCodes(transformer.getId()))
            .thenReturn(Set.of("SECONDARY_POLE"));

        assertThatThrownBy(() ->
            validator.validate(SePointType.START_POINT, feederPole, Optional.of(transformer)))
            .isInstanceOf(InvalidEquipmentSequenceException.class)
            .hasMessageContaining("not a legal continuation");
    }

    @Test
    void midPoint_afterPreviousEnd_skipsContinuityCheck() {
        // Continuity rule only applies to a form's Start Point, not Mid Point
        validator.validate(SePointType.MID_POINT, primaryPole, Optional.of(feederPole));
    }
}
