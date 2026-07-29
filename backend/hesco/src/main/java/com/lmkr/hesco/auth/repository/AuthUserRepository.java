package com.lmkr.hesco.auth.repository;

import com.lmkr.hesco.user.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Narrow, auth-scoped repository over the existing AppUser entity —
 * deliberately separate from user/repository/AppUserRepository (whose
 * exact contents this patch doesn't have visibility into) rather than
 * editing a file blind. Spring Data JPA supports multiple repository
 * interfaces over the same entity with no conflict.
 */
public interface AuthUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}