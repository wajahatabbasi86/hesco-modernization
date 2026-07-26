package com.lmkr.hesco.feeder.entity;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.gridstation.entity.GridStation;
import jakarta.persistence.*;

@Entity
@Table(name = "feeder")
public class Feeder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grid_station_id")
    private GridStation gridStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_division_id")
    private SubDivision subDivision;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Feeder() {
    }

    public Feeder(String code, String name, GridStation gridStation) {
        this.code = code;
        this.name = name;
        this.gridStation = gridStation;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GridStation getGridStation() { return gridStation; }
    public void setGridStation(GridStation gridStation) { this.gridStation = gridStation; }
    public SubDivision getSubDivision() { return subDivision; }
    public void setSubDivision(SubDivision subDivision) { this.subDivision = subDivision; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
