package com.company.leave.service;

import com.company.leave.dto.EmployeeRequest;
import com.company.leave.dto.EmployeeResponse;
import com.company.leave.entity.Employee;
import com.company.leave.exception.BusinessException;
import com.company.leave.exception.ResourceNotFoundException;
import com.company.leave.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void create_newEmail_savesEmployeeWithDefaultBalance() {
        EmployeeRequest request = new EmployeeRequest("Ravi", "ravi@company.com", "Engineering");
        when(employeeRepository.existsByEmail("ravi@company.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EmployeeResponse response = employeeService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.leaveBalance()).isEqualTo(20);
    }

    @Test
    void create_duplicateEmail_throwsBusinessException() {
        EmployeeRequest request = new EmployeeRequest("Ravi", "ravi@company.com", "Engineering");
        when(employeeRepository.existsByEmail("ravi@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void findEmployee_unknownId_throwsNotFound() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findEmployee(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Employee not found with id 42");
    }
}
