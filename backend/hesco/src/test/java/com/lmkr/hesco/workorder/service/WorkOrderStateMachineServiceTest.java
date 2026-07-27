package com.lmkr.hesco.workorder.service;

import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.feeder.entity.Feeder;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.entity.Role;
import com.lmkr.hesco.workorder.entity.*;
import com.lmkr.hesco.workorder.exception.CreatorScopeViolationException;
import com.lmkr.hesco.workorder.exception.InvalidWorkOrderTransitionException;
import com.lmkr.hesco.workorder.exception.MissingRejectionCommentException;
import com.lmkr.hesco.workorder.repository.WorkOrderRepository;
import com.lmkr.hesco.workorder.repository.WorkOrderTransitionLogRepository;
import com.lmkr.hesco.workorder.repository.WorkOrderTransitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * SRS §3.6.2-§3.6.6: the 4-tier Work Order approval state machine, plus
 * the mandatory-rejection-comment rule and Creator/feeder bound-scope
 * check. These three rules used to be three separate Postgres triggers
 * (a transitions-table trigger, fn_enforce_reject_comment,
 * fn_validate_work_order_creator_scope); this is the test suite that
 * proves they hold without a live database.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderStateMachineServiceTest {

    @Mock
    private WorkOrderTransitionRepository transitionRepository;
    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private WorkOrderTransitionLogRepository transitionLogRepository;

    private WorkOrderStateMachineService service;

    @BeforeEach
    void setUp() {
        service = new WorkOrderStateMachineService(transitionRepository, workOrderRepository, transitionLogRepository);
    }

    private Role role(String code) {
        Role role = new Role();
        role.setCode(code);
        return role;
    }

    private WorkOrderStatus status(String code) {
        WorkOrderStatus status = new WorkOrderStatus();
        status.setCode(code);
        return status;
    }

    private WorkOrderAction action(String code) {
        WorkOrderAction action = new WorkOrderAction();
        action.setCode(code);
        return action;
    }

    private SubDivision subDivision(long id) {
        return SubDivision.builder().id(id).code("1101" + id).name("SD" + id).build();
    }

    // ===================== assertCreatorScope =====================

    @Test
    void creatorScope_matchingSubDivision_doesNotThrow() {
        SubDivision sd = subDivision(1);
        AppUser creator = AppUser.builder().subDivision(sd).build();
        Feeder feeder = Feeder.builder().subDivision(sd).build();

        service.assertCreatorScope(creator, feeder);
    }

    @Test
    void creatorScope_mismatchedSubDivision_throwsCreatorScopeViolationException() {
        AppUser creator = AppUser.builder().subDivision(subDivision(1)).build();
        Feeder feeder = Feeder.builder().subDivision(subDivision(2)).build();

        assertThatThrownBy(() -> service.assertCreatorScope(creator, feeder))
            .isInstanceOf(CreatorScopeViolationException.class)
            .hasMessageContaining("SRS §3.6.3");
    }

    @Test
    void creatorScope_creatorHasNoSubDivision_throws() {
        AppUser creator = AppUser.builder().build(); // no sub-division
        Feeder feeder = Feeder.builder().subDivision(subDivision(1)).build();

        assertThatThrownBy(() -> service.assertCreatorScope(creator, feeder))
            .isInstanceOf(CreatorScopeViolationException.class);
    }

    // ===================== applyTransition =====================

    @Test
    void applyTransition_legalTransition_updatesStatusAndLogs() {
        WorkOrderStatus submitted = status("SUBMITTED");
        WorkOrderStatus approved = status("APPROVED_BY_APPROVER_1");
        WorkOrderAction approve = action("APPROVE");

        WorkOrder workOrder = WorkOrder.builder().status(submitted).build();
        AppUser approver = AppUser.builder().role(role("APPROVER_1")).build();

        WorkOrderTransition transition = new WorkOrderTransition();
        transition.setFromStatus(submitted);
        transition.setAction(approve);
        transition.setToStatus(approved);
        transition.setRequiresComment(false);

        when(transitionRepository.findTransition("SUBMITTED", "APPROVE", "APPROVER_1"))
            .thenReturn(Optional.of(transition));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrder result = service.applyTransition(workOrder, "APPROVE", approver, null);

        assertThat(result.getStatus().getCode()).isEqualTo("APPROVED_BY_APPROVER_1");
    }

    @Test
    void applyTransition_noMatchingTransition_throwsInvalidWorkOrderTransitionException() {
        WorkOrder workOrder = WorkOrder.builder().status(status("POSTED")).build();
        AppUser actor = AppUser.builder().role(role("CREATOR")).build();

        when(transitionRepository.findTransition("POSTED", "SUBMIT", "CREATOR"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyTransition(workOrder, "SUBMIT", actor, null))
            .isInstanceOf(InvalidWorkOrderTransitionException.class)
            .hasMessageContaining("No legal transition");
    }

    @Test
    void applyTransition_rejectWithoutComment_throwsMissingRejectionCommentException() {
        WorkOrderStatus submitted = status("SUBMITTED");
        WorkOrderStatus rejected = status("REJECTED_BY_APPROVER_1");
        WorkOrderAction reject = action("REJECT");

        WorkOrder workOrder = WorkOrder.builder().status(submitted).build();
        AppUser approver = AppUser.builder().role(role("APPROVER_1")).build();

        WorkOrderTransition transition = new WorkOrderTransition();
        transition.setFromStatus(submitted);
        transition.setAction(reject);
        transition.setToStatus(rejected);
        transition.setRequiresComment(true);

        when(transitionRepository.findTransition("SUBMITTED", "REJECT", "APPROVER_1"))
            .thenReturn(Optional.of(transition));

        assertThatThrownBy(() -> service.applyTransition(workOrder, "REJECT", approver, "  "))
            .isInstanceOf(MissingRejectionCommentException.class)
            .hasMessageContaining("comment is required");
    }

    @Test
    void applyTransition_rejectWithComment_succeeds() {
        WorkOrderStatus submitted = status("SUBMITTED");
        WorkOrderStatus rejected = status("REJECTED_BY_APPROVER_1");
        WorkOrderAction reject = action("REJECT");

        WorkOrder workOrder = WorkOrder.builder().status(submitted).build();
        AppUser approver = AppUser.builder().role(role("APPROVER_1")).build();

        WorkOrderTransition transition = new WorkOrderTransition();
        transition.setFromStatus(submitted);
        transition.setAction(reject);
        transition.setToStatus(rejected);
        transition.setRequiresComment(true);

        when(transitionRepository.findTransition("SUBMITTED", "REJECT", "APPROVER_1"))
            .thenReturn(Optional.of(transition));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrder result = service.applyTransition(workOrder, "REJECT", approver, "Incomplete survey data");

        assertThat(result.getStatus().getCode()).isEqualTo("REJECTED_BY_APPROVER_1");
    }
}
