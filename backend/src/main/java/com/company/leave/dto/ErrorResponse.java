package com.company.leave.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Every error from our API has this same JSON shape, so the frontend can handle errors in one way. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
}
