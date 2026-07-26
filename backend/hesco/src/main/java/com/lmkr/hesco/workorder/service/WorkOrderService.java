package com.lmkr.hesco.workorder.service;

import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.feeder.repository.FeederRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.workorder.api.dto.WorkOrderCreateRequest;
import com.lmkr.hesco.workorder.api.dto.WorkOrderResponse;
import com.lmkr.hesco.workorder.api.dto.WorkOrderTransitionRequest;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import com.lmkr.hesco.workorder.entity.WorkOrderStatus;
import com.lmkr.hesco.workorder.entity.WorkOrderType;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import com.lmkr.hesco.workorder.repository.WorkOrderStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class WorkOrderService {

    private static final String INITIAL_STATUS_CODE = "CREATED";

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusRepository statusRepository;
    private final FeederRepository feederRepository;
    private final AppUserRepository userRepository;
    private final WorkOrderStateMachineService stateMachineService;

    // ===============================
    // READ
    // ===============================

    public List<WorkOrder> findAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder findById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work Order not found: " + id));
    }

    // ===============================
    // CREATE (NO TRANSITION LOG HERE)
    // ===============================

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request) {

        // 1. Resolve entities
        Feeder feeder = feederRepository.findById(request.feederId())
                .orElseThrow(() -> new EntityNotFoundException("Feeder not found: " + request.feederId()));

        AppUser creator = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.createdByUserId()));

        // 2. Validate scope
        stateMachineService.assertCreatorScope(creator, feeder);

        // 3. Resolve initial status (SYSTEM CONTROLLED)
        WorkOrderStatus initialStatus = statusRepository.findByCode(INITIAL_STATUS_CODE)
                .orElseThrow(() ->
                        new EntityNotFoundException("WorkOrder status not seeded: " + INITIAL_STATUS_CODE));

        // 4. Resolve type (ENUM)
        WorkOrderType type = WorkOrderType.valueOf(request.woType());

        // 5. Build entity
        WorkOrder workOrder = WorkOrder.builder()
                .feeder(feeder)
                .woType(type)
                .status(initialStatus)
                .assignedTo(creator)
                .locationLat(request.locationLat())
                .locationLng(request.locationLng())
                .build();

        // 6. Save
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    // ===============================
    // TRANSITION (STATE MACHINE ONLY)
    // ===============================

    @Transactional
    public WorkOrderResponse transition(Long workOrderId, WorkOrderTransitionRequest request) {

        WorkOrder workOrder = findById(workOrderId);

        AppUser actor = userRepository.findById(request.actorUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found: " + request.actorUserId()));

        WorkOrder updated = stateMachineService.applyTransition(
                workOrder,
                request.actionCode(),   // IMPORTANT → use actionCode (not ID)
                actor,
                request.comment()
        );

        return WorkOrderResponse.from(updated);
    }
}