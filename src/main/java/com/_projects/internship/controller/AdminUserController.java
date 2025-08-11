package com._projects.internship.controller;

import com._projects.internship.dto.auth.RegisterRequest;
import com._projects.internship.dto.auth.UpdateUserRequest;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.SectorRepository;
import com._projects.internship.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/users")
@RequiredArgsConstructor
@Tag(name = "admin-user-controller", description = "Administration des utilisateurs")
public class AdminUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SectorRepository sectorRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Récupérer tous les utilisateurs",
            description = "Permet à un administrateur de récupérer tous les utilisateurs ou filtrer par rôle. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) Role role) {
        List<User> users = (role == null) ? userService.getAllUsers() : userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer un utilisateur par ID",
            description = "Permet de récupérer les détails d'un utilisateur spécifique. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur trouvé")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Créer un nouvel utilisateur",
            description = "Permet à un administrateur de créer un nouvel utilisateur dans le système. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<User> createUser(@RequestBody @Valid RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .sector(sectorRepository.findById(request.getSectorId())
                        .orElseThrow(() -> new com._projects.internship.exceptions.core.ResourceNotFoundException("Sector not found with ID: " + request.getSectorId())))
                .build();
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Modifier un utilisateur",
            description = "Permet à un administrateur de modifier les informations d'un utilisateur. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur modifié avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Supprimer un utilisateur",
            description = "Permet à un administrateur de supprimer un utilisateur du système. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis ou tentative d'auto-suppression")
    @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}