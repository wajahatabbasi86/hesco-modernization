package com.lmkr.hesco.survey.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

}
