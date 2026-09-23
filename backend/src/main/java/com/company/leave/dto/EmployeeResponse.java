package com.company.leave.dto;

import com.company.leave.entity.Employee;

/** DTO = what the API RETURNS for an employee. */
public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String department,
        Integer leaveBalance
) {
    /** Converts entity -> DTO. (Big projects often use MapStruct for this.) */
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmail(), e.getDepartment(), e.getLeaveBalance());
    }
}
