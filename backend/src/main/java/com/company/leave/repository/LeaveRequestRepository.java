package com.company.leave.repository;

import com.company.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // SELECT * FROM leave_requests WHERE employee_id = ? ORDER BY created_at DESC
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // SELECT * FROM leave_requests ORDER BY created_at DESC
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
}
