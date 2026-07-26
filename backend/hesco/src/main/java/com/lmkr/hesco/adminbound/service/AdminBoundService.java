package com.lmkr.hesco.adminbound.service;

import com.lmkr.hesco.adminbound.api.dto.CircleRequest;
import com.lmkr.hesco.adminbound.api.dto.CircleResponse;
import com.lmkr.hesco.adminbound.api.dto.DivisionRequest;
import com.lmkr.hesco.adminbound.api.dto.DivisionResponse;
import com.lmkr.hesco.adminbound.api.dto.SubDivisionRequest;
import com.lmkr.hesco.adminbound.api.dto.SubDivisionResponse;
import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.exception.DependentRecordsExistException;
import com.lmkr.hesco.adminbound.repository.CircleRepository;
import com.lmkr.hesco.adminbound.repository.DivisionRepository;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class AdminBoundService {

    private final AdminBoundCodeValidator codeValidator;
    private final AdminBoundDependencyRepository dependencyRepository;

    private final CircleRepository circleRepository;
    private final DivisionRepository divisionRepository;
    private final SubDivisionRepository subDivisionRepository;

    // ========================= CIRCLE =========================

    @Transactional(readOnly = true)
    public List<CircleResponse> getAllCircles() {
        return circleRepository.findAll()
                .stream()
                .map(CircleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Circle getCircle(Long id) {
        return circleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Circle not found: " + id));
    }

    @Transactional
    public CircleResponse createCircle(@Valid CircleRequest request) {
        Circle circle = Circle.builder()
                .code(request.code())
                .name(request.name())
                .build();

        return CircleResponse.from(circleRepository.save(circle));
    }

    @Transactional
    public CircleResponse updateCircle(Long id, CircleRequest request) {
        Circle circle = getCircle(id);
        circle.setCode(request.code());
        circle.setName(request.name());
        return CircleResponse.from(circleRepository.save(circle));
    }

    @Transactional
    public void deleteCircle(Long id) {
        Circle circle = getCircle(id);
        assertDeletable(circle);
        circleRepository.delete(circle);
    }

    // ========================= DIVISION =========================

    @Transactional(readOnly = true)
    public List<DivisionResponse> getDivisions(Long circleId) {
        List<Division> divisions = (circleId != null)
                ? divisionRepository.findByCircleId(circleId)
                : divisionRepository.findAll();

        return divisions.stream()
                .map(DivisionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DivisionResponse getDivision(Long id) {
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Division not found: " + id));

        return DivisionResponse.from(division);
    }

    @Transactional
    public DivisionResponse createDivision(Long circleId, String code, String name) {
        Circle circle = getCircle(circleId);
        codeValidator.validateDivisionCode(code, circle);

        Division division = Division.builder()
                .circle(circle)
                .code(code)
                .name(name)
                .build();
        return DivisionResponse.from(divisionRepository.save(division));
    }

    @Transactional
    public DivisionResponse updateDivision(Long id, DivisionRequest request) {
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Division not found: " + id));

        Circle circle = getCircle(request.circleId());
        codeValidator.validateDivisionCode(request.code(), circle);

        division.setCircle(circle);
        division.setCode(request.code());
        division.setName(request.name());

        return DivisionResponse.from(divisionRepository.save(division));
    }

    @Transactional
    public void deleteDivision(Long id) {
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Division not found: " + id));

        assertDeletable(division);
        divisionRepository.delete(division);
    }

    // ========================= SUBDIVISION =========================

    @Transactional(readOnly = true)
    public List<SubDivisionResponse> getSubDivisions(Long divisionId) {
        List<SubDivision> subDivisions = (divisionId != null)
                ? subDivisionRepository.findByDivisionId(divisionId)
                : subDivisionRepository.findAll();

        return subDivisions.stream()
                .map(SubDivisionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubDivisionResponse getSubDivision(Long id) {
        SubDivision subDivision = subDivisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + id));

        return SubDivisionResponse.from(subDivision);
    }

    @Transactional
    public SubDivisionResponse createSubDivision(Long divisionId, String code, String name) {
        Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new EntityNotFoundException("Division not found: " + divisionId));

        codeValidator.validateSubDivisionCode(code, division);

        SubDivision subDivision = SubDivision.builder()
                .division(division)
                .code(code)
                .name(name)
                .build();
        return SubDivisionResponse.from(subDivisionRepository.save(subDivision));
    }

    @Transactional
    public SubDivisionResponse updateSubDivision(Long id, SubDivisionRequest request) {
        SubDivision subDivision = subDivisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + id));

        Division division = divisionRepository.findById(request.divisionId())
                .orElseThrow(() -> new EntityNotFoundException("Division not found: " + request.divisionId()));

        codeValidator.validateSubDivisionCode(request.code(), division);

        subDivision.setDivision(division);
        subDivision.setCode(request.code());
        subDivision.setName(request.name());

        return SubDivisionResponse.from(subDivisionRepository.save(subDivision));
    }

    @Transactional
    public void deleteSubDivision(Long id) {
        SubDivision subDivision = subDivisionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + id));

        assertDeletable(subDivision);
        subDivisionRepository.delete(subDivision);
    }

    // ========================= DEPENDENCY CHECKS =========================

    public void assertDeletable(SubDivision subDivision) {
        long users = dependencyRepository.countUsersInSubDivision(subDivision.getId());
        long feeders = dependencyRepository.countFeedersInSubDivision(subDivision.getId());
        long workOrders = dependencyRepository.countWorkOrdersInSubDivision(subDivision.getId());

        if (users + feeders + workOrders > 0) {
            throw new DependentRecordsExistException(
                    String.format("Cannot delete SubDivision %s: %d users, %d feeders, %d work orders.",
                            subDivision.getCode(), users, feeders, workOrders));
        }
    }

    public void assertDeletable(Division division) {
        long users = dependencyRepository.countUsersInDivision(division.getId());
        long subDivisions = dependencyRepository.countSubDivisionsInDivision(division.getId());

        if (users + subDivisions > 0) {
            throw new DependentRecordsExistException(
                    String.format("Cannot delete Division %s: %d users, %d sub-divisions.",
                            division.getCode(), users, subDivisions));
        }
    }

    public void assertDeletable(Circle circle) {
        long users = dependencyRepository.countUsersInCircle(circle.getId());
        long divisions = dependencyRepository.countDivisionsInCircle(circle.getId());

        if (users + divisions > 0) {
            throw new DependentRecordsExistException(
                    String.format("Cannot delete Circle %s: %d users, %d divisions.",
                            circle.getCode(), users, divisions));
        }
    }
}