package com._projects.internship.service.chat;

import com._projects.internship.dto.chat.ConversationDTO;
import com._projects.internship.model.security.User;
import com._projects.internship.model.security.Role;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing chat participants based on business rules.
 * This service is modular and can be easily adapted to different business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Get list of users that the current user can chat with based on business rules:
     * - Students can only chat with companies they have applied to
     * - Companies can only chat with students who have applied to their offers
     * - Admins can chat with everyone (if needed)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAvailableParticipants(User currentUser) {
        List<Map<String, Object>> participants = new ArrayList<>();
        
        try {
            if (currentUser.getRole() == Role.STUDENT) {
                // Get companies that the student has applied to
                List<Long> companyIds = applicationRepository.findByStudentId(currentUser.getId())
                    .stream()
                    .map(app -> app.getOffer().getCompany().getId())
                    .distinct()
                    .collect(Collectors.toList());
                
                if (!companyIds.isEmpty()) {
                    List<User> companies = userRepository.findAllById(companyIds);
                    participants = companies.stream()
                        .filter(company -> company.getRole() == Role.COMPANY)
                        .map(this::mapUserToParticipant)
                        .collect(Collectors.toList());
                }
                
            } else if (currentUser.getRole() == Role.COMPANY) {
                // Get students who have applied to this company's offers
                List<Long> studentIds = applicationRepository.findByOfferCompanyId(currentUser.getId())
                    .stream()
                    .map(app -> app.getStudent().getId())
                    .distinct()
                    .collect(Collectors.toList());
                
                if (!studentIds.isEmpty()) {
                    List<User> students = userRepository.findAllById(studentIds);
                    participants = students.stream()
                        .filter(student -> student.getRole() == Role.STUDENT)
                        .map(this::mapUserToParticipant)
                        .collect(Collectors.toList());
                }
                
            } else if (currentUser.getRole() == Role.ADMIN) {
                // Admins can potentially chat with everyone (optional)
                // Uncomment if needed:
                // List<User> allUsers = userRepository.findAll();
                // participants = allUsers.stream()
                //     .filter(user -> !user.getId().equals(currentUser.getId()))
                //     .map(this::mapUserToParticipant)
                //     .collect(Collectors.toList());
            }
            
            log.debug("Found {} available participants for user {}", participants.size(), currentUser.getUsername());
            
        } catch (Exception e) {
            log.error("Error getting available participants for user {}: {}", currentUser.getUsername(), e.getMessage());
        }
        
        return participants;
    }

    /**
     * Check if two users can chat based on business rules
     */
    @Override
    public boolean canUsersChat(User user1, User user2) {
        // Same user cannot chat with themselves
        if (user1.getId().equals(user2.getId())) {
            return false;
        }

        // Admin can chat with anyone (optional)
        if (user1.getRole() == Role.ADMIN || user2.getRole() == Role.ADMIN) {
            return true;
        }

        // Student-Company chat rules
        if ((user1.getRole() == Role.STUDENT && user2.getRole() == Role.COMPANY) ||
            (user1.getRole() == Role.COMPANY && user2.getRole() == Role.STUDENT)) {
            
            User student = user1.getRole() == Role.STUDENT ? user1 : user2;
            User company = user1.getRole() == Role.COMPANY ? user1 : user2;
            
            // Check if student has applied to any of the company's offers
            return applicationRepository.existsByStudentIdAndOfferCompanyId(
                student.getId(), company.getId()
            );
        }

        // Other role combinations are not allowed
        return false;
    }

    /**
     * Get eligible participants for a user with additional filtering
     */
    @Override
    public List<ConversationDTO.ParticipantDTO> getEligibleParticipants(User currentUser) {
        List<ConversationDTO.ParticipantDTO> participants = new ArrayList<>();
        
        try {
            List<User> eligibleUsers = new ArrayList<>();
            
            if (currentUser.getRole() == Role.STUDENT) {
                // Get companies the student has applied to
                List<Long> companyIds = applicationRepository.findByStudentId(currentUser.getId())
                    .stream()
                    .map(app -> app.getOffer().getCompany().getId())
                    .distinct()
                    .collect(Collectors.toList());
                if (!companyIds.isEmpty()) {
                    eligibleUsers = userRepository.findAllById(companyIds).stream()
                        .filter(u -> u.getRole() == Role.COMPANY)
                        .collect(Collectors.toList());
                }
                
            } else if (currentUser.getRole() == Role.COMPANY) {
                // Get students who applied to this company's offers
                List<Long> studentIds = applicationRepository.findByOfferCompanyId(currentUser.getId())
                    .stream()
                    .map(app -> app.getStudent().getId())
                    .distinct()
                    .collect(Collectors.toList());
                if (!studentIds.isEmpty()) {
                    eligibleUsers = userRepository.findAllById(studentIds).stream()
                        .filter(u -> u.getRole() == Role.STUDENT)
                        .collect(Collectors.toList());
                }
            }
            
            // Convert to DTOs
            participants = eligibleUsers.stream()
                .map(user -> ConversationDTO.ParticipantDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .isOnline(false) // Would be determined by presence service
                    .lastSeen(null) // Would be tracked separately
                    .build())
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error getting eligible participants: {}", e.getMessage());
        }
        
        return participants;
    }

    /**
     * Map User entity to participant map
     */
    private Map<String, Object> mapUserToParticipant(User user) {
        Map<String, Object> participant = new HashMap<>();
        participant.put("id", user.getId());
        participant.put("username", user.getUsername());
        participant.put("email", user.getEmail());
        participant.put("fullName", user.getFirstName() + " " + user.getLastName());
        participant.put("role", user.getRole().name());
        participant.put("isOnline", false); // This would be determined by a presence service
        participant.put("lastSeen", null); // This would be tracked separately
        return participant;
    }
}