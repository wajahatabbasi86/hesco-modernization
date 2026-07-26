package com.lmkr.hesco.adminbound.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sub_division")
public class SubDivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

    @Column(nullable = false, unique = true, length = 5)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected SubDivision() {
    }

    public SubDivision(Division division, String code, String name) {
        this.division = division;
        this.code = code;
        this.name = name;
    }

    public Long getId() { return id; }
    public Division getDivision() { return division; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
