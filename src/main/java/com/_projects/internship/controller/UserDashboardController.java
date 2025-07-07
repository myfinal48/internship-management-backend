package com._projects.internship.controller;

import com._projects.internship.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/user/dashboard") // change "user" in endpoint depending on role
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('COMPANY') or hasRole('STUDENT') or hasRole('TEACHER')") // Allow all existing roles to access
public class UserDashboardController {

    private final DashboardService dashboardService;

}