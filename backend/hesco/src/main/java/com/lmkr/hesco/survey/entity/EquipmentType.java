package com.lmkr.hesco.survey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_type")
public class EquipmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // FEEDER_POLE, PRIMARY_POLE, TRANSFORMER, SECONDARY_POLE, METER

    @Column(name = "can_be_start", nullable = false)
    private boolean canBeStart;

    @Column(name = "can_be_end", nullable = false)
    private boolean canBeEnd;

    protected EquipmentType() {
    }

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public boolean isCanBeStart() { return canBeStart; }
    public boolean isCanBeEnd() { return canBeEnd; }
}
