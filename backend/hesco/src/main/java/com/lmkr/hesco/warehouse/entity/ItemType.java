package com.lmkr.hesco.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single value within an ItemCategory (SRS §3.5.2/§3.5.3) — e.g.
 * KVA_10 / "10 KVA" under TRANSFORMER_CAPACITY. sort_order controls fixed
 * column ordering in reports-service DTOs (SRS §3.15.2).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "item_type", uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "code"}))
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ItemCategory category;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "display_label", nullable = false, length = 150)
    private String displayLabel;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
