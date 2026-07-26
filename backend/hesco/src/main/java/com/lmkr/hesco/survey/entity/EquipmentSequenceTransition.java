package com.lmkr.hesco.survey.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_sequence_transition")
public class EquipmentSequenceTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_end_equipment_id", nullable = false)
    private EquipmentType fromEndEquipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_start_equipment_id", nullable = false)
    private EquipmentType toStartEquipment;

    protected EquipmentSequenceTransition() {
    }

    public EquipmentType getFromEndEquipment() { return fromEndEquipment; }
    public EquipmentType getToStartEquipment() { return toStartEquipment; }
}
