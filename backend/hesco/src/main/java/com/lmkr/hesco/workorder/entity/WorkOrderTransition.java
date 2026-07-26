package com.lmkr.hesco.workorder.entity;

import com.lmkr.hesco.user.entity.Role;
import jakarta.persistence.*;

/**
 * One legal (from_status, action, role) -> to_status row. Plain reference
 * data — WorkOrderStateMachineService reads it, it does not enforce
 * anything by itself (no DB trigger sits behind this table).
 */
@Entity
@Table(name = "work_order_transition")
public class WorkOrderTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_status_id", nullable = false)
    private WorkOrderStatus fromStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private WorkOrderAction action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_status_id", nullable = false)
    private WorkOrderStatus toStatus;

    @Column(name = "requires_comment", nullable = false)
    private boolean requiresComment;

    protected WorkOrderTransition() {
    }

    public WorkOrderStatus getFromStatus() { return fromStatus; }
    public WorkOrderAction getAction() { return action; }
    public Role getRole() { return role; }
    public WorkOrderStatus getToStatus() { return toStatus; }
    public boolean isRequiresComment() { return requiresComment; }
}
