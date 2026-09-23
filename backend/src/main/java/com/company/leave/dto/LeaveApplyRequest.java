package com.company.leave.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Request body for applying for leave. Dates come as "2026-10-05". */
public record LeaveApplyRequest(

        @NotNull(message = "Employee id is required")
        Long employeeId,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason must be at most 255 characters")
        String reason
) {
}
