package com.lmkr.hesco.workorder.entity;

import com.lmkr.hesco.user.entity.AppUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * One row per successfully applied transition — backs the Status-wise
 * Logs (SRS §3.15.3.2) and Feeder-wise Logs (§3.15.3.1) reports. Written
 * by WorkOrderStateMachineService in the same transaction as the
 * work_order.status update; there is no DB trigger writing this table.
 */
@Entity
@Table(name = "work_order_transition_log")
public class WorkOrderTransitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_status_id", nullable = false)
    private WorkOrderStatus fromStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private WorkOrderAction action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_status_id", nullable = false)
    private WorkOrderStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by", nullable = false)
    private AppUser performedBy;

    @Column(columnDefinition = "text")
    private String comment;

    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt;

    protected WorkOrderTransitionLog() {
    }

    public WorkOrderTransitionLog(WorkOrder workOrder, WorkOrderStatus fromStatus, WorkOrderAction action,
                                   WorkOrderStatus toStatus, AppUser performedBy, String comment) {
        this.workOrder = workOrder;
        this.fromStatus = fromStatus;
        this.action = action;
        this.toStatus = toStatus;
        this.performedBy = performedBy;
        this.comment = comment;
        this.performedAt = OffsetDateTime.now();
    }
}
