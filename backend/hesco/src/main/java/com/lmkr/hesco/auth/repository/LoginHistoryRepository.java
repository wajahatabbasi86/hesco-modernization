package com.lmkr.hesco.auth.repository;

import com.lmkr.hesco.auth.entity.LoginHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserIdOrderByLoginAtDesc(Long userId, Pageable pageable);
}