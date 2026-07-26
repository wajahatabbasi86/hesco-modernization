package com.lmkr.hesco.gridstation.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "power_transformer")
public class PowerTransformer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grid_station_id", nullable = false)
    private GridStation gridStation;

    @Column(name = "transformer_name", nullable = false, length = 100)
    private String transformerName;

    @Column(name = "cable_size", length = 50)
    private String cableSize;

    @Column(name = "ct_ratio", length = 20)
    private String ctRatio;

    @Column(name = "capacity_kva", precision = 10, scale = 2)
    private BigDecimal capacityKva;
}
