package com.company.leave.service;

import com.company.leave.dto.LeaveApplyRequest;
import com.company.leave.dto.LeaveResponse;
import com.company.leave.entity.Employee;
import com.company.leave.entity.LeaveRequest;
import com.company.leave.entity.LeaveStatus;
import com.company.leave.exception.BusinessException;
import com.company.leave.exception.ResourceNotFoundException;
import com.company.leave.repository.LeaveRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Business rules for leave:
 *  1. End date must not be before start date.
 *  2. Employee must have enough leave balance.
 *  3. Only PENDING leaves can be approved or rejected.
 *  4. When a leave is APPROVED, days are deducted from the employee's balance.
 */
@Service
public class LeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveService.class);

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeService employeeService;

    public LeaveService(LeaveRequestRepository leaveRepository, EmployeeService employeeService) {
        this.leaveRepository = leaveRepository;
        this.employeeService = employeeService;
    }

    @Transactional
    public LeaveResponse apply(LeaveApplyRequest request) {
        log.info("Leave apply request employeeId={} from={} to={}",
                request.employeeId(), request.startDate(), request.endDate());

        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("End date cannot be before start date");
        }

        Employee employee = employeeService.findEmployee(request.employeeId());

        // +1 because 5th to 5th is 1 day of leave
        int days = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;

        if (days > employee.getLeaveBalance()) {
            throw new BusinessException("Insufficient leave balance. Requested " + days
                    + " day(s), available " + employee.getLeaveBalance());
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setDays(days);
        leave.setReason(request.reason());
        leave.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRepository.save(leave);
        log.info("Leave created id={} days={}", saved.getId(), days);
        return LeaveResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaves(Long employeeId) {
        List<LeaveRequest> leaves = (employeeId == null)
                ? leaveRepository.findAllByOrderByCreatedAtDesc()
                : leaveRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        return leaves.stream().map(LeaveResponse::from).toList();
    }

    /**
     * @Transactional: status update + balance deduction happen together.
     * If anything fails in between, BOTH are rolled back. Balance never gets out of sync.
     */
    @Transactional
    public LeaveResponse approve(Long leaveId) {
        LeaveRequest leave = findPendingLeave(leaveId);
        Employee employee = leave.getEmployee();

        // Check again: balance may have changed since the leave was applied
        if (leave.getDays() > employee.getLeaveBalance()) {
            throw new BusinessException("Employee does not have enough leave balance to approve this leave");
        }

        employee.setLeaveBalance(employee.getLeaveBalance() - leave.getDays());
        leave.setStatus(LeaveStatus.APPROVED);
        // No save() needed: inside a transaction, JPA automatically saves changes to loaded entities ("dirty checking")

        log.info("Leave approved id={} employeeId={} newBalance={}",
                leaveId, employee.getId(), employee.getLeaveBalance());
        return LeaveResponse.from(leave);
    }

    @Transactional
    public LeaveResponse reject(Long leaveId) {
        LeaveRequest leave = findPendingLeave(leaveId);
        leave.setStatus(LeaveStatus.REJECTED);
        log.info("Leave rejected id={}", leaveId);
        return LeaveResponse.from(leave);
    }

    private LeaveRequest findPendingLeave(Long leaveId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id " + leaveId));
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Only PENDING leave can be updated. Current status: " + leave.getStatus());
        }
        return leave;
    }
}
