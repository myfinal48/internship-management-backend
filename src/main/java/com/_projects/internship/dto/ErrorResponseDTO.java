package com._projects.internship.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    
    public static ErrorResponseDTO forbidden(String message) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(403)
                .error("Forbidden")
                .message(message)
                .build();
    }
}