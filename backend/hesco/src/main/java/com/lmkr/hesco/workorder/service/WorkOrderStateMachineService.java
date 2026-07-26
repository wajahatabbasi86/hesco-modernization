package com.lmkr.hesco.workorder.service;

import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.workorder.entity.WorkOrder;
import com.lmkr.hesco.workorder.entity.WorkOrderTransition;
import com.lmkr.hesco.workorder.entity.WorkOrderTransitionLog;
import com.lmkr.hesco.workorder.exception.CreatorScopeViolationException;
import com.lmkr.hesco.workorder.exception.InvalidWorkOrderTransitionException;
import com.lmkr.hesco.workorder.exception.MissingRejectionCommentException;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import com.lmkr.hesco.workorder.repository.WorkOrderTransitionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The 4-tier Work Order approval state machine (SRS §3.6.2-§3.6.6),
 * entirely in Java. Replaces what used to be a Postgres trigger design
 * (a state-machine-as-table read by a trigger, plus a separate
 * fn_enforce_reject_comment trigger and fn_validate_work_order_creator_scope
 * trigger). Now:
 *
 *   - work_order_transition (04_work_order_and_survey.sql) is PLAIN
 *     reference data — this service is the only code that interprets it.
 *   - The mandatory-rejection-comment rule (SRS §3.6.4/5/6) is a plain
 *     `if` here instead of a DB trigger, so it's covered by a WorkOrderService
 *     unit test instead of needing a live Postgres instance to verify.
 *   - Creator bound-scope enforcement (SRS §3.6.3: "Creator shall create
 *     work orders only for feeders assigned to their Sub-Division") is a
 *     plain object comparison here instead of fn_validate_work_order_creator_scope.
 *
 * Every write to work_order.status_id MUST go through applyTransition() —
 * no controller/repository should call workOrderRepository.save() directly
 * after mutating status, or these rules can be silently bypassed (the same
 * risk that existed with triggers if a caller used raw SQL, just moved to
 * "don't bypass the service layer" instead of "don't bypass the DB").
 */
@Service
public class WorkOrderStateMachineService {

    private final WorkOrderTransitionRepository transitionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderTransitionLogRepository transitionLogRepository;

    public WorkOrderStateMachineService(WorkOrderTransitionRepository transitionRepository,
                                         WorkOrderRepository workOrderRepository,
                                         WorkOrderTransitionLogRepository transitionLogRepository) {
        this.transitionRepository = transitionRepository;
        this.workOrderRepository = workOrderRepository;
        this.transitionLogRepository = transitionLogRepository;
    }

    /**
     * SRS §3.6.3: "The system shall allow the Creator to create work
     * orders only for feeders assigned to their Sub-Division."
     */
    public void assertCreatorScope(AppUser creator, Feeder feeder) {
        if (creator.getSubDivision() == null || feeder.getSubDivision() == null
                || !creator.getSubDivision().getId().equals(feeder.getSubDivision().getId())) {
            throw new CreatorScopeViolationException(
                "Creator's Sub-Division does not match the feeder's Sub-Division (SRS §3.6.3)");
        }
    }

    @Transactional
    public WorkOrder applyTransition(WorkOrder workOrder, String actionCode, AppUser actor, String comment) {
        String currentStatusCode = workOrder.getStatus().getCode();
        String roleCode = actor.getRole().getCode();

        WorkOrderTransition transition = transitionRepository
            .findTransition(currentStatusCode, actionCode, roleCode)
            .orElseThrow(() -> new InvalidWorkOrderTransitionException(String.format(
                "No legal transition for action %s from status %s for role %s",
                actionCode, currentStatusCode, roleCode)));

        if (transition.isRequiresComment() && (comment == null || comment.isBlank())) {
            throw new MissingRejectionCommentException(
                "A comment is required for action " + actionCode + " (SRS §3.6.4/5/6)");
        }

        var fromStatus = workOrder.getStatus();
        workOrder.setStatus(transition.getToStatus());
        WorkOrder saved = workOrderRepository.save(workOrder);
        WorkOrderTransitionLog log = WorkOrderTransitionLog.builder()
                .workOrder(saved)
                .fromStatus(fromStatus)
                .action(transition.getAction())
                .toStatus(transition.getToStatus())
                .performedBy(actor)
                .comment(comment)
                .build();

        transitionLogRepository.save(log);

        return saved;
    }
}
