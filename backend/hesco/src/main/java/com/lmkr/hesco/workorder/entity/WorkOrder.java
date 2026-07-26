package com.lmkr.hesco.workorder.entity;

import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.user.entity.AppUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "work_order")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feeder_id", nullable = false)
    private Feeder feeder;

    @Enumerated(EnumType.STRING)
    @Column(name = "wo_type", nullable = false)
    private WorkOrderType woType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private WorkOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private AppUser assignedTo;

    private Double locationLat;
    private Double locationLng;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected WorkOrder() {
    }

    public WorkOrder(Feeder feeder, WorkOrderType woType, WorkOrderStatus initialStatus, AppUser createdBy) {
        this.feeder = feeder;
        this.woType = woType;
        this.status = initialStatus;
        this.createdBy = createdBy;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Feeder getFeeder() { return feeder; }
    public WorkOrderType getWoType() { return woType; }
    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }
    public AppUser getCreatedBy() { return createdBy; }
    public AppUser getAssignedTo() { return assignedTo; }
    public void setAssignedTo(AppUser assignedTo) { this.assignedTo = assignedTo; }
    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(Double locationLat) { this.locationLat = locationLat; }
    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(Double locationLng) { this.locationLng = locationLng; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
