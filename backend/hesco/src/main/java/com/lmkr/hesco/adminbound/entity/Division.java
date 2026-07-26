package com.lmkr.hesco.adminbound.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "division")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "circle_id", nullable = false)
    private Circle circle;

    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Division() {
    }

    public Division(Circle circle, String code, String name) {
        this.circle = circle;
        this.code = code;
        this.name = name;
    }

    public Long getId() { return id; }
    public Circle getCircle() { return circle; }
    public void setCircle(Circle circle) { this.circle = circle; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
