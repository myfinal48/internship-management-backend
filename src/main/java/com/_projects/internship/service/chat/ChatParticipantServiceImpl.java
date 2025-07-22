package com._projects.internship.service.chat;

import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private final UserService userService;
    private final ApplicationRepository applicationRepository;

    @Override
    public List<Map<String, Object>> getAvailableParticipants(User currentUser) {
        List<User> allowedUsers = new ArrayList<>();

        // Si l'utilisateur est un étudiant, il ne peut voir que les entreprises auxquelles il a postulé
        if (currentUser.getRole() == Role.STUDENT) {
            // Récupérer les IDs des entreprises auxquelles l'étudiant a postulé
            List<Long> companyIds = applicationRepository.findByStudentId(currentUser.getId())
                .stream()
                .map(app -> app.getInternshipOffer().getCompany().getId())
                .distinct()
                .toList();
            
            // Récupérer les entreprises correspondantes
            if (!companyIds.isEmpty()) {
                allowedUsers.addAll(userService.getUsersByRole(Role.COMPANY)
                    .stream()
                    .filter(company -> companyIds.contains(company.getId()))
                    .toList());
            }
        } 
        // Si l'utilisateur est une entreprise, elle ne peut voir que les étudiants qui ont postulé à ses offres
        else if (currentUser.getRole() == Role.COMPANY) {
            // Récupérer les IDs des étudiants qui ont postulé aux offres de l'entreprise
            List<Long> studentIds = applicationRepository.findByOfferCompanyId(currentUser.getId())
                .stream()
                .map(app -> app.getStudent().getId())
                .distinct()
                .toList();
            
            // Récupérer les étudiants correspondants
            if (!studentIds.isEmpty()) {
                allowedUsers.addAll(userService.getUsersByRole(Role.STUDENT)
                    .stream()
                    .filter(student -> studentIds.contains(student.getId()))
                    .toList());
            }
        } 
        // Pour les autres rôles (admin, teacher), montrer tous les utilisateurs
        else {
            allowedUsers.addAll(Stream.concat(
                userService.getUsersByRole(Role.COMPANY).stream(),
                userService.getUsersByRole(Role.STUDENT).stream()
            ).toList());
        }

        // Exclure l'utilisateur actuel et convertir en format de réponse
        return allowedUsers.stream()
            .filter(user -> !user.getId().equals(currentUser.getId()))
            .map(this::userResponseToParticipantMap)
            .toList();
    }
    
    private Map<String, Object> userResponseToParticipantMap(User user) {
        Map<String, Object> participantInfo = new HashMap<>();
        participantInfo.put("id", user.getId());
        participantInfo.put("fullName", user.getFirstName() + " " + user.getLastName());
        participantInfo.put("username", user.getUsername());
        return participantInfo;
    }
}