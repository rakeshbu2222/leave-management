package com.company.leave.controller;

import com.company.leave.dto.LeaveApplyRequest;
import com.company.leave.dto.LeaveResponse;
import com.company.leave.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // POST /api/leaves
    @PostMapping
    public ResponseEntity<LeaveResponse> apply(@Valid @RequestBody LeaveApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.apply(request));
    }

    // GET /api/leaves            -> all leaves
    // GET /api/leaves?employeeId=1 -> leaves of one employee
    @GetMapping
    public List<LeaveResponse> getLeaves(@RequestParam(required = false) Long employeeId) {
        return leaveService.getLeaves(employeeId);
    }

    // GET /api/leaves/10
    @GetMapping("/{id}")
    public LeaveResponse getById(@PathVariable Long id) {
        return leaveService.getById(id);
    }

    // PUT /api/leaves/10/approve
    @PutMapping("/{id}/approve")
    public LeaveResponse approve(@PathVariable Long id) {
        return leaveService.approve(id);
    }

    // PUT /api/leaves/10/reject
    @PutMapping("/{id}/reject")
    public LeaveResponse reject(@PathVariable Long id) {
        return leaveService.reject(id);
    }

    // PUT /api/leaves/10/cancel
    @PutMapping("/{id}/cancel")
    public LeaveResponse cancel(@PathVariable Long id) {
        return leaveService.cancel(id);
    }
}
