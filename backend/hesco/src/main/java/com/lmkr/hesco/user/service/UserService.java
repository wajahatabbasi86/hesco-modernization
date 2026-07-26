package com.lmkr.hesco.user.service;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.adminbound.repository.CircleRepository;
import com.lmkr.hesco.adminbound.repository.DivisionRepository;
import com.lmkr.hesco.adminbound.repository.SubDivisionRepository;
import com.lmkr.hesco.user.api.dto.AppUserRequest;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.entity.Role;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.user.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
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
@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final CircleRepository circleRepository;
    private final DivisionRepository divisionRepository;
    private final SubDivisionRepository subDivisionRepository;
    private final UserRoleBoundValidator roleBoundValidator;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(AppUserRepository appUserRepository, RoleRepository roleRepository,
                        CircleRepository circleRepository, DivisionRepository divisionRepository,
                        SubDivisionRepository subDivisionRepository,
                        UserRoleBoundValidator roleBoundValidator) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.circleRepository = circleRepository;
        this.divisionRepository = divisionRepository;
        this.subDivisionRepository = subDivisionRepository;
        this.roleBoundValidator = roleBoundValidator;
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll();
    }

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
