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
        EmployeeRequest request = new EmployeeRequest("Ravi", "ravi@company.com", "Engineering", null);
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
        EmployeeRequest request = new EmployeeRequest("Ravi", "ravi@company.com", "Engineering", null);
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

    @Test
    void create_mixedCaseEmailWithSpaces_savesTrimmedLowercase() {
        EmployeeRequest request = new EmployeeRequest("Ravi", "  Ravi@Company.COM ", "Engineering", null);
        when(employeeRepository.existsByEmail("ravi@company.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.create(request);

        assertThat(response.email()).isEqualTo("ravi@company.com");
    }

    @Test
    void create_sameEmailDifferentCase_throwsBusinessException() {
        EmployeeRequest request = new EmployeeRequest("Ravi", "RAVI@Company.com", "Engineering", null);
        when(employeeRepository.existsByEmail("ravi@company.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void create_withPhoneNumber_savesPhone() {
        EmployeeRequest request = new EmployeeRequest("Ravi", "ravi@company.com", "Engineering", "9876543210");
        when(employeeRepository.existsByEmail("ravi@company.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.create(request);

        assertThat(response.phoneNumber()).isEqualTo("9876543210");
    }
    
    @Test
    void update_validRequest_updatesFieldsWithoutCallingSave() {
        Employee existing = new Employee("Ravi", "ravi@company.com", "Engineering");
        existing.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailAndIdNot("ravi.k@company.com", 1L)).thenReturn(false);

        EmployeeResponse response = employeeService.update(1L,
                new EmployeeRequest("Ravi Kumar", " Ravi.K@Company.com ", "Platform", "9876543210"));

        assertThat(response.name()).isEqualTo("Ravi Kumar");
        assertThat(response.email()).isEqualTo("ravi.k@company.com");
        assertThat(response.department()).isEqualTo("Platform");
        assertThat(response.phoneNumber()).isEqualTo("9876543210");
        assertThat(response.leaveBalance()).isEqualTo(20);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void update_emailUsedByAnotherEmployee_throwsBusinessException() {
        Employee existing = new Employee("Ravi", "ravi@company.com", "Engineering");
        existing.setId(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailAndIdNot("priya@company.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(1L,
                new EmployeeRequest("Ravi", "priya@company.com", "Engineering", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
        assertThat(existing.getEmail()).isEqualTo("ravi@company.com");
    }

    @Test
    void update_unknownId_throwsNotFound() {
        when(employeeRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(42L,
                new EmployeeRequest("Ravi", "ravi@company.com", "Engineering", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
