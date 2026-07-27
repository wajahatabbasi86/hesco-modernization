package com.lmkr.hesco.workorder.service;

import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.feeder.repository.FeederRepository;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.repository.AppUserRepository;
import com.lmkr.hesco.workorder.api.dto.WorkOrderAssignRequest;
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
    private static final String ASSIGN_ACTION_CODE = "ASSIGN";

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusRepository statusRepository;
    private final FeederRepository feederRepository;
    private final AppUserRepository userRepository;
    private final WorkOrderStateMachineService stateMachineService;

    // ===============================
    // READ
    // ===============================

    /**
     * Returns DTOs, mapped inside this @Transactional method - the
     * previous findAll()/findById() (no transaction, entities returned
     * and mapped by the controller afterward) hit the same
     * LazyInitializationException class of bug as UserService.findAll()
     * did: WorkOrder.feeder/status/createdBy/assignedTo are all
     * FetchType.LAZY, and open-in-view is disabled.
     */
    @Transactional(readOnly = true)
    public List<WorkOrderResponse> findAllResponses() {
        return workOrderRepository.findAll().stream().map(WorkOrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse findResponseById(Long id) {
        return WorkOrderResponse.from(findById(id));
    }

    public List<WorkOrder> findAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder findById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work Order not found: " + id));
    }

    // ===============================
    // CREATE — status CREATED, no assignee yet (SRS §3.6.3: assigning to a
    // Surveyor is a separate step via assign(), which is what actually
    // fires the CREATED -> ASSIGNED transition; create() never touches
    // assignedTo or the state machine).
    // ===============================

    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request) {

        // 1. Resolve entities
        Feeder feeder = feederRepository.findById(request.feederId())
                .orElseThrow(() -> new EntityNotFoundException("Feeder not found: " + request.feederId()));

        AppUser creator = userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.createdByUserId()));

        // 2. Validate scope (SRS §3.6.3 — Creator may only create work orders
        //    for feeders assigned to their own Sub-Division)
        stateMachineService.assertCreatorScope(creator, feeder);

        // 3. Resolve initial status (SYSTEM CONTROLLED)
        WorkOrderStatus initialStatus = statusRepository.findByCode(INITIAL_STATUS_CODE)
                .orElseThrow(() ->
                        new EntityNotFoundException("WorkOrder status not seeded: " + INITIAL_STATUS_CODE));

        // 4. Resolve type (ENUM)
        WorkOrderType type = WorkOrderType.valueOf(request.woType());

        // 5. Build entity — deliberately no assignedTo here
        WorkOrder workOrder = WorkOrder.builder()
                .feeder(feeder)
                .woType(type)
                .status(initialStatus)
                .createdBy(creator)
                .locationLat(request.locationLat())
                .locationLng(request.locationLng())
                .build();

        // 6. Save
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    // ===============================
    // ASSIGN — Creator hands the work order to a Surveyor (SRS §3.6.3).
    // Sets assignedTo AND drives the CREATED -> ASSIGN -> ASSIGNED
    // transition through the state machine, in one transaction, so the
    // status and the assignee never get out of sync with each other.
    // ===============================

    @Transactional
    public WorkOrderResponse assign(Long workOrderId, WorkOrderAssignRequest request) {
        WorkOrder workOrder = findById(workOrderId);

        AppUser surveyor = userRepository.findById(request.surveyorUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.surveyorUserId()));

        AppUser actor = userRepository.findById(request.actorUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.actorUserId()));

        workOrder.setAssignedTo(surveyor);

        WorkOrder updated = stateMachineService.applyTransition(
                workOrder, ASSIGN_ACTION_CODE, actor, null);

        return WorkOrderResponse.from(updated);
    }

    // ===============================
    // TRANSITION (STATE MACHINE ONLY — every other action)
    // ===============================

    @Transactional
    public WorkOrderResponse transition(Long workOrderId, WorkOrderTransitionRequest request) {

        WorkOrder workOrder = findById(workOrderId);

        AppUser actor = userRepository.findById(request.actorUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found: " + request.actorUserId()));

        WorkOrder updated = stateMachineService.applyTransition(
                workOrder,
                request.actionCode(),
                actor,
                request.comment()
        );

        return WorkOrderResponse.from(updated);
    }
}