package com._projects.internship.controller;

import com._projects.internship.dto.auth.RegisterRequest;
import com._projects.internship.dto.auth.UpdateUserRequest;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.SectorRepository;
import com._projects.internship.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SectorRepository sectorRepository;

    // Inject the user service

    // GET /api/v1/admin/users - List users (optionally filtered by role)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN can access these
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) Role role) {
        List<User> users = (role == null) ? userService.getAllUsers() : userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // GET /api/v1/admin/users/{id} - Get a single user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id); // Assumes service throws exception if not found
        return ResponseEntity.ok(user);
    }

    // POST /api/v1/admin/users - Create a new user (TEMPORARY DIAGNOSTIC VERSION)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN can access these
    public ResponseEntity<User> createUser(@RequestBody RegisterRequest request) { // Temporarily remove RequestBody
        System.out.println(">>> DIAGNOSTIC: createUser endpoint reached by authenticated user."); // Add log
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encode password
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .sector(sectorRepository.findById(request.getSectorId()).get())
                .build();
        User createdUser = userService.createUser(user); // Temporarily comment out service call
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // Temporarily comment out original return
    }

    // PUT /api/v1/admin/users/{id} - Update an existing user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN can access these
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    // DELETE /api/v1/admin/users/{id} - Delete a user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN can access these
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Standard response for successful delete
    }
}