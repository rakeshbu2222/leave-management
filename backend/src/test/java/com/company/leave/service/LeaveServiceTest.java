package com.company.leave.service;

import com.company.leave.dto.LeaveApplyRequest;
import com.company.leave.dto.LeaveResponse;
import com.company.leave.entity.Employee;
import com.company.leave.entity.LeaveRequest;
import com.company.leave.entity.LeaveStatus;
import com.company.leave.exception.BusinessException;
import com.company.leave.exception.ResourceNotFoundException;
import com.company.leave.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT test: tests only LeaveService logic.
 * Repository and EmployeeService are MOCKED -> no database, no Spring, runs in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRepository;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private LeaveService leaveService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee("Ravi", "ravi@company.com", "Engineering");
        employee.setId(1L);
        employee.setLeaveBalance(10);
    }

    // ---------- apply ----------

    @Test
    void apply_validRequest_createsPendingLeave() {
        LocalDate start = LocalDate.now().plusDays(1);
        LeaveApplyRequest request = new LeaveApplyRequest(1L, start, start.plusDays(2), "Family function");

        when(employeeService.findEmployee(1L)).thenReturn(employee);
        // return the same object that was passed to save()
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveResponse response = leaveService.apply(request);

        assertThat(response.days()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        verify(leaveRepository).save(any(LeaveRequest.class));
    }

    @Test
    void apply_endDateBeforeStartDate_throwsBusinessException() {
        LocalDate start = LocalDate.now().plusDays(5);
        LeaveApplyRequest request = new LeaveApplyRequest(1L, start, start.minusDays(1), "Trip");

        assertThatThrownBy(() -> leaveService.apply(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("End date cannot be before start date");

        verify(leaveRepository, never()).save(any());
    }

    @Test
    void apply_notEnoughBalance_throwsBusinessException() {
        LocalDate start = LocalDate.now().plusDays(1);
        LeaveApplyRequest request = new LeaveApplyRequest(1L, start, start.plusDays(14), "Long trip"); // 15 days

        when(employeeService.findEmployee(1L)).thenReturn(employee);

        assertThatThrownBy(() -> leaveService.apply(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient leave balance");
    }

    @Test
    void apply_sameStartAndEndDate_countsAsOneDay() {   // edge case
        LocalDate day = LocalDate.now().plusDays(1);
        LeaveApplyRequest request = new LeaveApplyRequest(1L, day, day, "Doctor visit");

        when(employeeService.findEmployee(1L)).thenReturn(employee);
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(leaveService.apply(request).days()).isEqualTo(1);
    }

    // ---------- approve / reject ----------

    @Test
    void approve_pendingLeave_deductsBalance() {
        LeaveRequest leave = pendingLeave(3);
        when(leaveRepository.findById(100L)).thenReturn(Optional.of(leave));

        LeaveResponse response = leaveService.approve(100L);

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(employee.getLeaveBalance()).isEqualTo(7);
    }

    @Test
    void approve_alreadyApproved_throwsBusinessException() {
        LeaveRequest leave = pendingLeave(3);
        leave.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(100L)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> leaveService.approve(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PENDING");
        assertThat(employee.getLeaveBalance()).isEqualTo(10); // unchanged
    }

    @Test
    void approve_leaveNotFound_throwsNotFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.approve(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reject_pendingLeave_doesNotChangeBalance() {
        LeaveRequest leave = pendingLeave(3);
        when(leaveRepository.findById(100L)).thenReturn(Optional.of(leave));

        LeaveResponse response = leaveService.reject(100L);

        assertThat(response.status()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(employee.getLeaveBalance()).isEqualTo(10);
    }

    private LeaveRequest pendingLeave(int days) {
        LeaveRequest leave = new LeaveRequest();
        leave.setId(100L);
        leave.setEmployee(employee);
        leave.setStartDate(LocalDate.now().plusDays(1));
        leave.setEndDate(LocalDate.now().plusDays(days));
        leave.setDays(days);
        leave.setReason("Test");
        leave.setStatus(LeaveStatus.PENDING);
        return leave;
    }

    // ---------- getById ----------

    @Test
    void getById_existingLeave_returnsLeave() {
        LeaveRequest leave = new LeaveRequest();
        leave.setId(100L);
        leave.setEmployee(employee);
        leave.setDays(3);
        leave.setStatus(LeaveStatus.APPROVED);
        when(leaveRepository.findById(100L)).thenReturn(Optional.of(leave));

        LeaveResponse response = leaveService.getById(100L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.employeeName()).isEqualTo("Ravi");
        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
    }

    @Test
    void getById_unknownLeave_throwsNotFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Leave request not found with id 99");
    }
}
