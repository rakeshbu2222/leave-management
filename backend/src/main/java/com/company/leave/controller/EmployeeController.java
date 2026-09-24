package com.company.leave.controller;

import com.company.leave.dto.EmployeeRequest;
import com.company.leave.dto.EmployeeResponse;
import com.company.leave.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Employee-related REST APIs

/**
 * Controller = HTTP layer only. Receives request, validates it (@Valid), calls service, returns response.
 * NO business logic here.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // POST /api/employees  -> 201 Created
    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    // GET /api/employees
    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.getAll();
    }

    // GET /api/employees/5
    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }
}
