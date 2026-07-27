package com.lmkr.hesco.user.service;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.repository.CircleRepository;
import com.lmkr.hesco.adminbound.repository.DivisionRepository;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
import com.lmkr.hesco.user.api.dto.AppUserRequest;
import com.lmkr.hesco.user.api.dto.AppUserResponse;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.entity.Role;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.user.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestrates AppUser create/update: resolves role + admin-bound
 * references, runs UserRoleBoundValidator (SRS §3.2.3/§8.1.1) BEFORE
 * persisting, and hashes the password. Every write to app_user should go
 * through here rather than a controller calling the repository directly,
 * so the bound/IMEI rule can't be bypassed.
 */
@AllArgsConstructor
@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final CircleRepository circleRepository;
    private final DivisionRepository divisionRepository;
    private final SubDivisionRepository subDivisionRepository;
    private final UserRoleBoundValidator roleBoundValidator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Returns DTOs, not entities - mapped WHILE the Hibernate session is
     * still open (bound to this @Transactional method), so lazy
     * associations (role, circle, division, subDivision on AppUser are
     * all FetchType.LAZY) can be read here safely. Previously this
     * returned List<AppUser> and the controller did the
     * AppUserResponse::from mapping itself, AFTER the (untransactional)
     * repository call had already returned and the session had closed -
     * with open-in-view disabled (application.yml), that throws
     * LazyInitializationException the moment .getRole() is touched.
     * Matches the pattern AdminBoundService already uses (DTO mapping
     * happens inside the @Transactional method, not in the controller).
     */
    @Transactional(readOnly = true)
    public List<AppUserResponse> findAll() {
        return appUserRepository.findAll()
                .stream()
                .map(AppUserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppUserResponse findResponseById(Long id) {
        return AppUserResponse.from(findById(id));
    }

    /**
     * Returns the raw entity - only for internal use by other
     * @Transactional methods in this class (create/update), which stay
     * within the same session. Do NOT expose this to a controller without
     * mapping to a DTO first.
     */
    public AppUser findById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Transactional
    public AppUser create(AppUserRequest request) {
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + request.roleId()));

        AppUser user = AppUser.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .contactNumber(request.contactNumber())
                .role(role)
                .imei(request.imei())
                .build();
        applyBounds(user, request);

        roleBoundValidator.validate(user);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser update(Long id, AppUserRequest request) {
        AppUser user = findById(id);
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + request.roleId()));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setContactNumber(request.contactNumber());
        user.setRole(role);
        user.setImei(request.imei());
        applyBounds(user, request);

        roleBoundValidator.validate(user);
        return appUserRepository.save(user);
    }

    private void applyBounds(AppUser user, AppUserRequest request) {
        Circle circle = request.circleId() != null
                ? circleRepository.findById(request.circleId())
                .orElseThrow(() -> new EntityNotFoundException("Circle not found: " + request.circleId()))
                : null;
        Division division = request.divisionId() != null
                ? divisionRepository.findById(request.divisionId())
                .orElseThrow(() -> new EntityNotFoundException("Division not found: " + request.divisionId()))
                : null;
        SubDivision subDivision = request.subDivisionId() != null
                ? subDivisionRepository.findById(request.subDivisionId())
                .orElseThrow(() -> new EntityNotFoundException("Sub-Division not found: " + request.subDivisionId()))
                : null;

        user.setCircle(circle);
        user.setDivision(division);
        user.setSubDivision(subDivision);
    }
}