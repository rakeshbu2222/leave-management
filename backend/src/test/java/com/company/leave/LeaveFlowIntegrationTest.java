package com.company.leave;

import com.company.leave.entity.Employee;
import com.company.leave.repository.EmployeeRepository;
import com.company.leave.repository.LeaveRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTEGRATION test: starts the full app with a real (H2) database. Nothing is mocked.
 * Tests the full flow: Controller -> Service -> Repository -> DB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaveFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private LeaveRequestRepository leaveRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        leaveRepository.deleteAll();
        employeeRepository.deleteAll();
        employee = employeeRepository.save(new Employee("Test User", "test@company.com", "QA"));
    }

    @Test
    void applyThenApprove_deductsBalanceInDatabase() throws Exception {
        LocalDate start = LocalDate.now().plusDays(3);
        String body = """
                {"employeeId": %d, "startDate": "%s", "endDate": "%s", "reason": "Vacation"}
                """.formatted(employee.getId(), start, start.plusDays(4)); // 5 days

        String json = mockMvc.perform(post("/api/leaves").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long leaveId = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(put("/api/leaves/" + leaveId + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Employee reloaded = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(reloaded.getLeaveBalance()).isEqualTo(15);
    }

    @Test
    void createEmployee_duplicateEmail_returns400() throws Exception {
        String body = """
                {"name": "Another", "email": "test@company.com", "department": "QA"}
                """;
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_sameEmailDifferentCase_returns400() throws Exception {
        String body = """
                {"name": "Another", "email": "Test@Company.COM", "department": "QA"}
                """;
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee with email test@company.com already exists"));

        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    @Test
    void createEmployee_withValidPhone_returns201WithPhone() throws Exception {
        String body = """
                {"name": "Asha", "email": "asha@company.com", "department": "QA", "phoneNumber": "9876543210"}
                """;
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"));
    }

    @Test
    void createEmployee_withoutPhone_returns201WithNullPhone() throws Exception {
        String body = """
                {"name": "Asha", "email": "asha@company.com", "department": "QA"}
                """;
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phoneNumber").doesNotExist());
    }

    @Test
    void createEmployee_invalidPhone_returns400() throws Exception {
        String body = """
                {"name": "Asha", "email": "asha@company.com", "department": "QA", "phoneNumber": "12345"}
                """;
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("phoneNumber: Phone number must be exactly 10 digits"));
    }
}
