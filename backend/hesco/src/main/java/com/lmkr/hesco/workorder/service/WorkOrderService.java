package com.lmkr.hesco.workorder.service;

import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import com.lmkr.hesco.workorder.entity.WorkOrderStatus;
import com.lmkr.hesco.workorder.entity.WorkOrderType;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import com.lmkr.hesco.workorder.repository.WorkOrderStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Owns Work Order creation and every status transition (SRS §3.6). Every
 * create runs WorkOrderStateMachineService.assertCreatorScope (§3.6.3)
 * first; every status change goes through applyTransition() so the
 * 4-tier state machine, the mandatory-rejection-comment rule, and the
 * transition log can never be bypassed by a controller calling
 * WorkOrderRepository.save() directly with a mutated status.
 */
@Service
public class WorkOrderService {

    private static final String INITIAL_STATUS_CODE = "CREATED";

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusRepository workOrderStatusRepository;
    private final WorkOrderStateMachineService stateMachineService;

    public WorkOrderService(WorkOrderRepository workOrderRepository, WorkOrderStatusRepository workOrderStatusRepository,
                             WorkOrderStateMachineService stateMachineService) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderStatusRepository = workOrderStatusRepository;
        this.stateMachineService = stateMachineService;
    }

    public List<WorkOrder> findAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder findById(Long id) {
        return workOrderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Work Order not found: " + id));
    }

    @Transactional
    public WorkOrder create(Feeder feeder, WorkOrderType woType, AppUser creator, Double lat, Double lng) {
        stateMachineService.assertCreatorScope(creator, feeder);

        WorkOrderStatus initialStatus = workOrderStatusRepository.findByCode(INITIAL_STATUS_CODE)
            .orElseThrow(() -> new EntityNotFoundException("Work Order status not seeded: " + INITIAL_STATUS_CODE));

        WorkOrder workOrder = WorkOrder.builder()
                .feeder(feeder)
                .woType(woType)
                .status(initialStatus)
                .assignedTo(creator)
                .build();
        workOrder.setLocationLat(lat);
        workOrder.setLocationLng(lng);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder transition(Long workOrderId, String actionCode, AppUser actor, String comment) {
        WorkOrder workOrder = findById(workOrderId);
        return stateMachineService.applyTransition(workOrder, actionCode, actor, comment);
    }
}
