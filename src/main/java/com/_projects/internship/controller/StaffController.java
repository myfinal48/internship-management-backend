package com._projects.internship.controller;

import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.service.UserService;
import com._projects.internship.dto.user.UpdateProfileRequest;
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
@RequestMapping("${api.prefix}/staff") // New base path for staff-related info
@PreAuthorize("hasAnyRole('ADMIN', 'COMPANY', 'STUDENT', 'TEACHER')") // Allow all existing roles
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;

    // Endpoint specifically for getting users, accessible by multiple roles
    @GetMapping
    public ResponseEntity<List<User>> getAllStaff() {
        List<User> staff = userService.getAllUsers();
        // Consider returning a simpler DTO instead of the full User object if needed
        return ResponseEntity.ok(staff);
    }

    // Endpoint specifically for getting users, accessible by multiple roles
    @GetMapping("/{role}")
    public ResponseEntity<List<User>> getStaffByRole(@PathVariable Role role) {
        List<User> staff = userService.getUsersByRole(role);
        // Consider returning a simpler DTO instead of the full User object if needed
        return ResponseEntity.ok(staff);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
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

    // Could add endpoints for other roles or staff lists here if needed
}