package com.lmkr.hesco.adminbound.service;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.exception.InvalidCodeHierarchyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SRS §3.1.1 coding convention: Division code's first 3 digits must equal
 * the parent Circle's code; Sub-Division code's first 4 digits must equal
 * the parent Division's code. This is the rule that used to live in
 * trg_validate_division_code / trg_validate_sub_division_code (Postgres
 * triggers) — these tests are the payoff of moving it to Java: no live
 * database needed to prove the rule holds.
 */
class AdminBoundCodeValidatorTest {

    private final AdminBoundCodeValidator validator = new AdminBoundCodeValidator();

    @Test
    void validDivisionCode_matchingParentPrefix_isAccepted() {
        Circle circle = Circle.builder().code("110").name("Hyderabad Circle").build();

        // no exception thrown == pass
        validator.validateDivisionCode("1101", circle);
    }

    @Test
    void divisionCode_wrongPrefix_throwsInvalidCodeHierarchyException() {
        Circle circle = Circle.builder().code("110").name("Hyderabad Circle").build();

        assertThatThrownBy(() -> validator.validateDivisionCode("2201", circle))
            .isInstanceOf(InvalidCodeHierarchyException.class)
            .hasMessageContaining("must start with parent circle code");
    }

    @Test
    void divisionCode_wrongLength_throwsInvalidCodeHierarchyException() {
        Circle circle = Circle.builder().code("110").name("Hyderabad Circle").build();

        assertThatThrownBy(() -> validator.validateDivisionCode("11", circle))
            .isInstanceOf(InvalidCodeHierarchyException.class)
            .hasMessageContaining("exactly 4 digits");
    }

    @Test
    void validSubDivisionCode_matchingParentPrefix_isAccepted() {
        Circle circle = Circle.builder().code("110").name("Hyderabad Circle").build();
        Division division = Division.builder().circle(circle).code("1101").name("Hyderabad Division").build();

        validator.validateSubDivisionCode("11011", division);
    }

    @Test
    void subDivisionCode_wrongPrefix_throwsInvalidCodeHierarchyException() {
        Circle circle = Circle.builder().code("110").name("Hyderabad Circle").build();
        Division division = Division.builder().circle(circle).code("1101").name("Hyderabad Division").build();

        assertThatThrownBy(() -> validator.validateSubDivisionCode("99991", division))
            .isInstanceOf(InvalidCodeHierarchyException.class)
            .hasMessageContaining("must start with parent division code");
    }

    @Test
    void subDivisionCode_wrongLength_throwsInvalidCodeHierarchyException() {
        Circle circle = Circle.builder().code("110").name("Hyderabad Circle").build();
        Division division = Division.builder().circle(circle).code("1101").name("Hyderabad Division").build();

        assertThatThrownBy(() -> validator.validateSubDivisionCode("110111234", division))
            .isInstanceOf(InvalidCodeHierarchyException.class)
            .hasMessageContaining("exactly 5 digits");
    }
}
