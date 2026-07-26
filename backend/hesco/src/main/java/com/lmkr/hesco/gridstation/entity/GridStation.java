package com.lmkr.hesco.gridstation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "grid_station")
public class GridStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    private Double latitude;
    private Double longitude;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected GridStation() {
    }

    public GridStation(String code, String name, Double latitude, Double longitude) {
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
