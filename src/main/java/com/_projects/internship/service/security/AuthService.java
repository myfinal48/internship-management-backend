package com._projects.internship.service.security;

import com._projects.internship.dto.auth.AuthResponse;
import com._projects.internship.dto.auth.LoginRequest;
import com._projects.internship.dto.auth.RegisterRequest;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.SectorRepository;
import com._projects.internship.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final SectorRepository sectorRepository;

  /**
   * Registers a new user.
   *
   * @param request The registration request details.
   * @return AuthResponse containing the JWT token for the newly registered user.
   * @throws IllegalArgumentException if username or email already exists.
   */
  public AuthResponse register(RegisterRequest request) {
    // Check if username or email already exists
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new IllegalArgumentException("Username already exists");
    }
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }
    if(sectorRepository.findById(request.getSectorId()).isEmpty()){
      throw new IllegalArgumentException("Sector not found");
    }

    var user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword())) // Encode password
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .role(request.getRole())
        .sector(sectorRepository.findById(request.getSectorId()).get())
        .build();

    userRepository.save(user); // Save the new user

    // Generate JWT token for the new user
    var jwtToken = jwtService.generateToken(user);
    return AuthResponse.builder()
        .token(jwtToken)
        .build();
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param request The login request details.
   * @return AuthResponse containing the JWT token upon successful authentication.
   * @throws AuthenticationException if authentication fails.
   */
  public AuthResponse login(LoginRequest request) {
    // Authenticate the user using Spring Security's AuthenticationManager
    // This will use our UserDetailsServiceImpl and PasswordEncoder
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(), // Use email from LoginRequest
            request.getPassword()));

    // If authentication is successful, find the user by email
    var user = userRepository.findByEmail(request.getEmail()) // Find by email
        .orElseThrow(() -> new IllegalStateException(
            "User not found after successful authentication")); // Should not happen

    // Generate JWT token
    var jwtToken = jwtService.generateToken(user);
    return AuthResponse.builder()
        .token(jwtToken)
        .user(user) // Add the user object to the response
        .build();
  }
}