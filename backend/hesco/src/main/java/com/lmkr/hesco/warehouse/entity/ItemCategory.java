package com.lmkr.hesco.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Warehouse item category (SRS §3.5) — e.g. TRANSFORMER_CAPACITY,
 * PRIMARY_STRUCTURE, HT_CONDUCTOR. Backs the fixed enumerated lists used
 * by reports-service and the mobile survey form's dropdowns, per the
 * revamp plan §2.4 recommendation (configurable reference data instead of
 * hardcoded Java enums, satisfying the §6.4 NFR).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_category")
public class ItemCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
