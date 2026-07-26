package com.lmkr.hesco.user.repository;

import com.lmkr.hesco.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {
    Optional<Role> findByCode(String code);
}
