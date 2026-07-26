package com.lmkr.hesco.feeder.entity;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.user.entity.AppUser;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "feeder_assignment_log")
public class FeederAssignmentLog {

    public enum Action { ASSIGN, UNASSIGN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feeder_id", nullable = false)
    private Feeder feeder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_division_id")
    private SubDivision subDivision; // null row = "unassigned" event

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by", nullable = false)
    private AppUser performedBy;

    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt;

    protected FeederAssignmentLog() {
    }

    public FeederAssignmentLog(Feeder feeder, SubDivision subDivision, Action action, AppUser performedBy) {
        this.feeder = feeder;
        this.subDivision = subDivision;
        this.action = action;
        this.performedBy = performedBy;
        this.performedAt = OffsetDateTime.now();
    }
}
