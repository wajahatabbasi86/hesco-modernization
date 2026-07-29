package com.lmkr.hesco.auth.repository;

import com.lmkr.hesco.auth.entity.PasswordChangeAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordChangeAuditRepository extends JpaRepository<PasswordChangeAudit, Long> {
}