package com.company.leave.service;

import com.company.leave.dto.EmployeeRequest;
import com.company.leave.dto.EmployeeResponse;
import com.company.leave.entity.Employee;
import com.company.leave.exception.BusinessException;
import com.company.leave.exception.ResourceNotFoundException;
import com.company.leave.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Service layer = business logic lives here (rules, checks, calculations).
 * Controller only handles HTTP; Repository only talks to DB; Service sits in the middle.
 */
@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;

    // Constructor injection: Spring passes the repository in. Easy to mock in unit tests.
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        String email = normalizeEmail(request.email());
        log.info("Creating employee with email={}", email);

        if (employeeRepository.existsByEmail(email)) {
            throw new BusinessException(
                    "Employee with email " + email + " already exists"
            );
        }

        Employee employee = new Employee(
                request.name(),
                email,
                request.department()
        );

        employee.setPhoneNumber(request.phoneNumber());

        Employee saved = employeeRepository.save(employee);

        log.info("Employee created id={}", saved.getId());
        return EmployeeResponse.from(saved);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        String email = normalizeEmail(request.email());

        if (employeeRepository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException("Employee with email " + email + " already exists");
        }

        employee.setName(request.name());
        employee.setEmail(email);
        employee.setDepartment(request.department());
        employee.setPhoneNumber(request.phoneNumber());
        // No save(): the entity was loaded inside this transaction, so dirty checking writes the UPDATE on commit

        log.info("Employee updated id={}", id);
        return EmployeeResponse.from(employee);
    }

    /** Emails are case-insensitive: always compare and store them trimmed and lowercase. */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return EmployeeResponse.from(findEmployee(id));
    }

    /** Shared helper - also used by LeaveService. */
    public Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + id));
    }
}
