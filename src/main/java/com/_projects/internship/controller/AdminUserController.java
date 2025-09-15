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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/users")
@RequiredArgsConstructor
@Tag(name = "admin-user-controller", description = "User Administration")
public class AdminUserController {

    private final UserService userService;
    private final SectorRepository sectorRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Retrieve all users",
            description = "Allows an administrator to retrieve all users or filter by role. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "List of users retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) Role role) {
        List<User> users = (role == null) ? userService.getAllUsers() : userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve user by ID",
            description = "Allows retrieving details of a specific user. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new user",
            description = "Allows an administrator to create a new user in the system. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @ApiResponse(responseCode = "400", description = "Invalid data")
    public ResponseEntity<User> createUser(@RequestBody @Valid RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
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
            summary = "Update a user",
            description = "Allows an administrator to modify a user's information. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a user",
            description = "Allows an administrator to delete a user from the system. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required or self-deletion attempt")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}