package com._projects.internship.controller;

import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.service.UserService;
import com._projects.internship.dto.user.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/staff")
@RequiredArgsConstructor
@Tag(name = "staff-controller", description = "Gestion du personnel et profils utilisateurs")
public class StaffController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Récupérer tout le personnel",
            description = "Permet de récupérer la liste de tous les utilisateurs du système. Accessible à tous."
    )
    @ApiResponse(responseCode = "200", description = "Liste du personnel récupérée")
    public ResponseEntity<List<User>> getAllStaff() {
        List<User> staff = userService.getAllUsers();
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/{role}")
    @Operation(
            summary = "Récupérer le personnel par rôle",
            description = "Permet de récupérer tous les utilisateurs ayant un rôle spécifique (ADMIN, TEACHER, COMPANY, STUDENT). Accessible à tous."
    )
    @ApiResponse(responseCode = "200", description = "Personnel filtré par rôle récupéré")
    public ResponseEntity<List<User>> getStaffByRole(@PathVariable Role role) {
        List<User> staff = userService.getUsersByRole(role);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Récupérer mon profil",
            description = "Permet à un utilisateur connecté de récupérer ses informations de profil. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Profil utilisateur récupéré")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mettre à jour mon profil",
            description = "Permet à un utilisateur connecté de modifier ses informations de profil. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Profil mis à jour avec succès")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest updateRequest) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        userService.updateProfile(user.getId(), updateRequest.getUsername(), updateRequest.getFirstName(), updateRequest.getLastName(), updateRequest.getSectorId());
        return ResponseEntity.ok("Profile updated successfully");
    }
}