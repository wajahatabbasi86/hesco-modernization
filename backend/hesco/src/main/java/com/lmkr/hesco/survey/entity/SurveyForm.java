package com.lmkr.hesco.survey.entity;

import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "survey_form")
public class SurveyForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "se_point", nullable = false)
    private SePointType sePoint;

    @Column(name = "gps_number", nullable = false, unique = true, length = 30)
    private String gpsNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "line_length_meters", precision = 10, scale = 2)
    private BigDecimal lineLengthMeters;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private AppUser submittedBy;

    private Double latitude;
    private Double longitude;
    private String remarks;

    @Column(name = "synced_at")
    private OffsetDateTime syncedAt;

    protected SurveyForm() {
    }

    public SurveyForm(WorkOrder workOrder, SePointType sePoint, String gpsNumber, EquipmentType equipmentType,
                       BigDecimal lineLengthMeters, AppUser submittedBy, Double latitude, Double longitude,
                       String remarks) {
        this.workOrder = workOrder;
        this.sePoint = sePoint;
        this.gpsNumber = gpsNumber;
        this.equipmentType = equipmentType;
        this.lineLengthMeters = lineLengthMeters;
        this.submittedBy = submittedBy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.remarks = remarks;
        this.syncedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public WorkOrder getWorkOrder() { return workOrder; }
    public SePointType getSePoint() { return sePoint; }
    public String getGpsNumber() { return gpsNumber; }
    public EquipmentType getEquipmentType() { return equipmentType; }
    public BigDecimal getLineLengthMeters() { return lineLengthMeters; }
    public AppUser getSubmittedBy() { return submittedBy; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getRemarks() { return remarks; }
    public OffsetDateTime getSyncedAt() { return syncedAt; }
}
