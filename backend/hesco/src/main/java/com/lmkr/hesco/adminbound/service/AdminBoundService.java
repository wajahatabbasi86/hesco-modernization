package com.lmkr.hesco.adminbound.service;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.exception.DependentRecordsExistException;
import org.springframework.stereotype.Service;

/**
 * Owns Admin Bound create/update/delete (SRS §3.1.2-3.1.5). Every write
 * path here is the only allowed entry point to the circle/division/
 * sub_division tables — no direct repository.save() from a controller —
 * so AdminBoundCodeValidator and the deletion guard below can never be
 * bypassed the way a trigger sometimes can be with a bulk/raw SQL write.
 */
@Service
public class AdminBoundService {

    private final AdminBoundCodeValidator codeValidator;
    private final AdminBoundDependencyRepository dependencyRepository;

    public AdminBoundService(AdminBoundCodeValidator codeValidator,
                              AdminBoundDependencyRepository dependencyRepository) {
        this.codeValidator = codeValidator;
        this.dependencyRepository = dependencyRepository;
    }

    public void validateNewDivision(String code, Circle parentCircle) {
        codeValidator.validateDivisionCode(code, parentCircle);
    }

    public void validateNewSubDivision(String code, Division parentDivision) {
        codeValidator.validateSubDivisionCode(code, parentDivision);
    }

    /**
     * Throws DependentRecordsExistException with a message naming exactly
     * which dependent counts are non-zero (SRS §3.1.5: "the message to the
     * user needs to say why"), instead of letting the DB's ON DELETE
     * RESTRICT surface as a raw FK-violation error.
     */
    public void assertDeletable(SubDivision subDivision) {
        long users = dependencyRepository.countUsersInSubDivision(subDivision.getId());
        long feeders = dependencyRepository.countFeedersInSubDivision(subDivision.getId());
        long workOrders = dependencyRepository.countWorkOrdersInSubDivision(subDivision.getId());
        if (users + feeders + workOrders > 0) {
            throw new DependentRecordsExistException(String.format(
                "Cannot delete Sub-Division %s: %d users, %d feeders, %d work orders assigned.",
                subDivision.getCode(), users, feeders, workOrders));
        }
    }

    public void assertDeletable(Division division) {
        long users = dependencyRepository.countUsersInDivision(division.getId());
        long subDivisions = dependencyRepository.countSubDivisionsInDivision(division.getId());
        if (users + subDivisions > 0) {
            throw new DependentRecordsExistException(String.format(
                "Cannot delete Division %s: %d users, %d sub-divisions assigned.",
                division.getCode(), users, subDivisions));
        }
    }

    public void assertDeletable(Circle circle) {
        long users = dependencyRepository.countUsersInCircle(circle.getId());
        long divisions = dependencyRepository.countDivisionsInCircle(circle.getId());
        if (users + divisions > 0) {
            throw new DependentRecordsExistException(String.format(
                "Cannot delete Circle %s: %d users, %d divisions assigned.",
                circle.getCode(), users, divisions));
        }
    }
}
