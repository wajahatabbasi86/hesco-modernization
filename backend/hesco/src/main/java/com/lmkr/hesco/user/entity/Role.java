package com.lmkr.hesco.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_bound_type", nullable = false)
    private BoundType assignedBoundType;

    @Column(name = "requires_imei", nullable = false)
    private boolean requiresImei;

    protected Role() {
    }

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public BoundType getAssignedBoundType() { return assignedBoundType; }
    public boolean isRequiresImei() { return requiresImei; }
}
