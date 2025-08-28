package com._projects.internship.dto.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String firstName;
    private String lastName;
    private Long sectorId;
} 