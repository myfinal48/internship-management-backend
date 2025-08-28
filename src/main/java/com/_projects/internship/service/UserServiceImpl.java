package com._projects.internship.service;

import com._projects.internship.dto.auth.UpdateUserRequest;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.exceptions.core.SelfDeletionException;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.SectorRepository;
import com._projects.internship.repository.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SectorRepository sectorRepository;

  @Override
  @Transactional
  public User createUser(User user) {
    Objects.requireNonNull(user, "User cannot be null");
    if (!StringUtils.hasText(user.getPassword())) {
      throw new IllegalArgumentException("Password cannot be empty for new user");
    }
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists: " + user.getEmail());
    }
    if (StringUtils.hasText(user.getUsername()) && userRepository.findByUsername(user.getUsername()).isPresent()) {
      throw new IllegalArgumentException("Username already exists: " + user.getUsername());
    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setId(null);
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public User updateUser(Long userId, UpdateUserRequest userUpdates) {
    Objects.requireNonNull(userId, "User ID cannot be null for update");
    Objects.requireNonNull(userUpdates, "User updates cannot be null");

    User existingUser = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

    existingUser.setFirstName(userUpdates.getFirstName());
    existingUser.setLastName(userUpdates.getLastName());
    existingUser.setRole(userUpdates.getRole());
    existingUser.setEmail(userUpdates.getEmail());
    existingUser.setSector(sectorRepository.findById(userUpdates.getSectorId())
        .orElseThrow(() -> new ResourceNotFoundException("Sector not found")));
    existingUser.setUsername(userUpdates.getUsername());

    return userRepository.save(existingUser);
  }

  @Override
  @Transactional
  public void deleteUser(Long userId, String currentUsername) {
    User userToDelete = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    
    if (userToDelete.getUsername().equals(currentUsername)) {
      throw new SelfDeletionException("Cannot delete your own account");
    }
    
    userRepository.deleteById(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<User> getUsersByRole(Role role) {
    Objects.requireNonNull(role, "Role cannot be null for filtering");
    return userRepository.findByRole(role);
  }

  @Override
  @Transactional(readOnly = true)
  public User findByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
  }

  @Override
  @Transactional
  public User updateProfile(Long userId, String username, String firstName, String lastName, Long sectorId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    if (username != null && !username.isBlank()) user.setUsername(username);
    if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
    if (lastName != null && !lastName.isBlank()) user.setLastName(lastName);
    if (sectorId != null) {
      user.setSector(sectorRepository.findById(sectorId)
        .orElseThrow(() -> new ResourceNotFoundException("Sector not found")));
    }
    return userRepository.save(user);
  }
}