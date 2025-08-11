package com._projects.internship.service;

import com._projects.internship.dto.auth.UpdateUserRequest;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User updateUser(Long userId, UpdateUserRequest user);

    void deleteUser(Long userId, String currentUsername);

    User getUserById(Long userId);

    List<User> getAllUsers();

    List<User> getUsersByRole(Role role);

    User findByUsername(String username);

    User updateProfile(Long userId, String username, String firstName, String lastName, Long sectorId);
}