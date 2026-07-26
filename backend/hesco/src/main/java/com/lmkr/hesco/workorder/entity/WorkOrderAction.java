package com.lmkr.hesco.workorder.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "work_order_action")
public class WorkOrderAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // ASSIGN, COMPLETE_SURVEY, VALIDATE, REVERT, SUBMIT, APPROVE, REJECT, POST, DELETE

    protected WorkOrderAction() {
    }

    public Short getId() { return id; }
    public String getCode() { return code; }
}
