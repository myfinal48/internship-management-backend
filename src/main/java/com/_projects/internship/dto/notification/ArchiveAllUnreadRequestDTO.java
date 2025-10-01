package com._projects.internship.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveAllUnreadRequestDTO {
    
    @NotNull(message = "User ID is required")
    private Long userId;
}
