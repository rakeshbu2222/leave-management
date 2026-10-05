package com.company.leave.controller;

import com.company.leave.dto.LeaveResponse;
import com.company.leave.entity.LeaveStatus;
import com.company.leave.exception.BusinessException;
import com.company.leave.exception.ResourceNotFoundException;
import com.company.leave.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CONTROLLER test: loads only the web layer (controller + exception handler + JSON).
 * Service is mocked.
 *
 * We test:
 * - URL mapping
 * - Status codes
 * - Validation
 * - JSON shape
 */
@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Test
    void apply_validBody_returns201() throws Exception {

        LocalDate start = LocalDate.now().plusDays(1);

        LeaveResponse response = new LeaveResponse(
                10L,
                1L,
                "Ravi",
                start,
                start.plusDays(1),
                2,
                "Trip",
                LeaveStatus.PENDING,
                LocalDateTime.now()
        );

        when(leaveService.apply(any())).thenReturn(response);

        String body = """
                {
                    "employeeId": 1,
                    "startDate": "%s",
                    "endDate": "%s",
                    "reason": "Trip"
                }
                """.formatted(start, start.plusDays(1));

        mockMvc.perform(
                        post("/api/leaves")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void apply_missingFields_returns400WithDetails() throws Exception {

        mockMvc.perform(
                        post("/api/leaves")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.length()").value(4));
    }

    @Test
    void approve_businessRuleFails_returns400() throws Exception {

        when(leaveService.approve(5L))
                .thenThrow(
                        new BusinessException(
                                "Only PENDING leave can be updated"
                        )
                );

        mockMvc.perform(
                        put("/api/leaves/5/approve")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value("Only PENDING leave can be updated")
                );
    }

    @Test
    void approve_unknownLeave_returns404() throws Exception {

        when(leaveService.approve(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Leave request not found with id 99"
                        )
                );

        mockMvc.perform(
                        put("/api/leaves/99/approve")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_nonNumericId_returns400() throws Exception {

        mockMvc.perform(
                        put("/api/leaves/abc/approve")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid value 'abc' for parameter 'id'. Expected Long"
                                )
                );

        verifyNoInteractions(leaveService);
    }

    @Test
    void getLeaves_nonNumericEmployeeId_returns400() throws Exception {

        mockMvc.perform(
                        get("/api/leaves")
                                .param("employeeId", "xyz")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid value 'xyz' for parameter 'employeeId'. Expected Long"
                                )
                );

        verifyNoInteractions(leaveService);
    }

    @Test
    void approve_wrongHttpMethod_returns405() throws Exception {

        mockMvc.perform(
                        post("/api/leaves/5/approve")
                )
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getById_existingLeave_returns200() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LeaveResponse response = new LeaveResponse(10L, 1L, "Ravi", start, start.plusDays(1), 2,
                "Trip", LeaveStatus.PENDING, LocalDateTime.now());
        when(leaveService.getById(10L)).thenReturn(response);

        mockMvc.perform(get("/api/leaves/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.employeeName").value("Ravi"));
    }

    @Test
    void getById_unknownLeave_returns404() throws Exception {
        when(leaveService.getById(99L)).thenThrow(new ResourceNotFoundException("Leave request not found with id 99"));

        mockMvc.perform(get("/api/leaves/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Leave request not found with id 99"));
    }

    @Test
    void cancel_businessRuleFails_returns400() throws Exception {
        when(leaveService.cancel(5L)).thenThrow(new BusinessException("Cannot cancel a leave with status REJECTED"));

        mockMvc.perform(put("/api/leaves/5/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot cancel a leave with status REJECTED"));
    }
}
