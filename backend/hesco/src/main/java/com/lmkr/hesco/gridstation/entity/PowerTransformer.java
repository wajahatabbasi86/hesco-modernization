package com.lmkr.hesco.gridstation.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

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

    protected PowerTransformer() {
    }

    public PowerTransformer(GridStation gridStation, String transformerName, String cableSize,
                             String ctRatio, BigDecimal capacityKva) {
        this.gridStation = gridStation;
        this.transformerName = transformerName;
        this.cableSize = cableSize;
        this.ctRatio = ctRatio;
        this.capacityKva = capacityKva;
    }

    public Long getId() { return id; }
    public GridStation getGridStation() { return gridStation; }
    public String getTransformerName() { return transformerName; }
    public void setTransformerName(String transformerName) { this.transformerName = transformerName; }
    public String getCableSize() { return cableSize; }
    public void setCableSize(String cableSize) { this.cableSize = cableSize; }
    public String getCtRatio() { return ctRatio; }
    public void setCtRatio(String ctRatio) { this.ctRatio = ctRatio; }
    public BigDecimal getCapacityKva() { return capacityKva; }
    public void setCapacityKva(BigDecimal capacityKva) { this.capacityKva = capacityKva; }
}
