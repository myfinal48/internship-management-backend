package com._projects.internship.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour représenter un enseignant dans l'interface utilisateur.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
}