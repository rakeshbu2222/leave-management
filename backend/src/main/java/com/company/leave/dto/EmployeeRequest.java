package com.company.leave.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO = what the client SENDS to create an employee.
 * Notice: no id, no leaveBalance -> the client cannot set those. That is one big reason we use DTOs.
 * A Java "record" is a short way to write an immutable class with fields + getters + constructor.
 */
public record EmployeeRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Department is required")
        String department,

        @Pattern(regexp = "\\d{10}", message = "Phone number must be exactly 10 digits")
        String phoneNumber
) {
}
