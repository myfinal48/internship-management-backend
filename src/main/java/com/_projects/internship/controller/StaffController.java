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
@Tag(name = "staff-controller", description = "Staff management and user profiles")
public class StaffController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Retrieve all staff",
            description = "Allows retrieving the list of all users in the system. Accessible to everyone."
    )
    @ApiResponse(responseCode = "200", description = "Staff list retrieved")
    public ResponseEntity<List<User>> getAllStaff() {
        List<User> staff = userService.getAllUsers();
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/{role}")
    @Operation(
            summary = "Retrieve staff by role",
            description = "Allows retrieving all users with a specific role (ADMIN, TEACHER, COMPANY, STUDENT). Accessible to everyone."
    )
    @ApiResponse(responseCode = "200", description = "Staff filtered by role retrieved")
    public ResponseEntity<List<User>> getStaffByRole(@PathVariable Role role) {
        List<User> staff = userService.getUsersByRole(role);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Retrieve my profile",
            description = "Allows a logged-in user to retrieve their profile information. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "User profile retrieved")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
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
            summary = "Update my profile",
            description = "Allows a logged-in user to modify their profile information. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
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