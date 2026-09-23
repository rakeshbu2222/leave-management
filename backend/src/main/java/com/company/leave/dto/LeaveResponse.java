package com.company.leave.dto;

import com.company.leave.entity.LeaveRequest;
import com.company.leave.entity.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        Integer days,
        String reason,
        LeaveStatus status,
        LocalDateTime createdAt
) {
    public static LeaveResponse from(LeaveRequest l) {
        return new LeaveResponse(
                l.getId(),
                l.getEmployee().getId(),
                l.getEmployee().getName(),
                l.getStartDate(),
                l.getEndDate(),
                l.getDays(),
                l.getReason(),
                l.getStatus(),
                l.getCreatedAt()
        );
    }
}
