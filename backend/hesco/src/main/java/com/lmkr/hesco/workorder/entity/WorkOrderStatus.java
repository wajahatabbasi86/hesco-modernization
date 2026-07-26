package com.lmkr.hesco.workorder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "work_order_status")
public class WorkOrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    protected WorkOrderStatus() {
    }

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
}
