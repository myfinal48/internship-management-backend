package com._projects.internship.service.notification;

import com._projects.internship.dto.notification.NotificationRequestDTO;
import com._projects.internship.model.core.Application;
import com._projects.internship.model.core.Convention;
import com._projects.internship.model.core.InternshipOffer;
import com._projects.internship.model.notification.NotificationChannel;
import com._projects.internship.model.notification.NotificationType;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * Helper class for sending notifications in different business scenarios
 */
@Component
@RequiredArgsConstructor
public class NotificationHelper {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Sends a notification to all users in the sector of a new internship offer
     * @param offer The new internship offer
     * @param sender The user who created the offer
     */
    public void notifyNewOffer(InternshipOffer offer, User sender) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType(NotificationType.NEW_OFFER);
        request.setChannel(NotificationChannel.IN_APP);
        request.setSenderId(sender.getId());
        request.setSubject("Nouvelle offre de stage disponible");
        request.setContent("Une nouvelle offre de stage est disponible : " + offer.getTitle());
        request.setTargetRole(Role.STUDENT);
        request.setSector(offer.getSector());
        
        notificationService.sendNotification(request);
    }

    /**
     * Notifies a company when a student applies to their internship offer
     * @param application The application submitted
     * @param sender The student who applied
     */
    public void notifyNewApplication(Application application, User sender) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType(NotificationType.NEW_APPLICATION);
        request.setChannel(NotificationChannel.IN_APP);
        request.setSenderId(sender.getId());
        request.setSubject("Nouvelle candidature reçue");
        request.setContent("Un étudiant a postulé à votre offre : " + application.getInternshipOffer().getTitle());
        request.setUserIds(Collections.singleton(application.getInternshipOffer().getCompany().getId()));
        
        notificationService.sendNotification(request);
    }

    /**
     * Notifies a student when their application status changes
     * @param application The application with updated status
     * @param sender The company user who made the decision
     */
    public void notifyApplicationDecision(Application application, User sender) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType(NotificationType.APPLICATION_DECISION_REMINDER);
        request.setChannel(NotificationChannel.IN_APP);
        request.setSenderId(sender.getId());
        
        String status = application.getStatus().toString();
        String title = application.getInternshipOffer().getTitle();
        
        request.setSubject("Décision sur votre candidature");
        request.setContent("Votre candidature pour l'offre \"" + title + "\" a été " + 
                (status.equals("ACCEPTED") ? "acceptée" : "refusée"));
        request.setUserIds(Collections.singleton(application.getStudent().getId()));
        
        notificationService.sendNotification(request);
    }

    /**
     * Notifies teachers in the relevant sector when a new convention is created
     * @param convention The newly created convention
     * @param sender The company user who created the convention
     */
    public void notifyNewConvention(Convention convention, User sender) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType(NotificationType.CONVENTION_VALIDATION);
        request.setChannel(NotificationChannel.IN_APP);
        request.setSenderId(sender.getId());
        request.setSubject("Nouvelle convention à valider");
        request.setContent("Une nouvelle convention a été créée pour l'offre : " + 
                convention.getInternshipOffer().getTitle());
        request.setTargetRole(Role.TEACHER);
        request.setSector(convention.getInternshipOffer().getSector());
        
        notificationService.sendNotification(request);
    }

    /**
     * Notifies admins when a teacher validates a convention
     * @param convention The validated convention
     * @param sender The teacher who validated
     */
    public void notifyConventionValidatedByTeacher(Convention convention, User sender) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType(NotificationType.ADMIN_APPROVAL);
        request.setChannel(NotificationChannel.IN_APP);
        request.setSenderId(sender.getId());
        request.setSubject("Convention validée par un enseignant");
        request.setContent("Une convention a été validée par un enseignant et nécessite votre approbation");
        request.setTargetRole(Role.ADMIN);
        
        notificationService.sendNotification(request);
    }

    /**
     * Notifies a company when a teacher rejects their convention
     * @param convention The rejected convention
     * @param sender The teacher who rejected
     */
    public void notifyConventionRejectedByTeacher(Convention convention, User sender) {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setType(NotificationType.CONVENTION_VALIDATION);
        request.setChannel(NotificationChannel.IN_APP);
        request.setSenderId(sender.getId());
        request.setSubject("Convention rejetée par un enseignant");
        request.setContent("Votre convention a été rejetée par un enseignant. Raison : " + 
                convention.getRejectionReason());
        request.setUserIds(Collections.singleton(convention.getCompany().getId()));
        
        notificationService.sendNotification(request);
    }

    /**
     * Notifies relevant parties when an admin makes a decision on a convention
     * @param convention The convention with admin decision
     * @param sender The admin who made the decision
     */
    public void notifyConventionAdminDecision(Convention convention, User sender) {
        boolean isApproved = convention.getStatus().toString().contains("APPROVED");
        
        // Notify company in all cases
        NotificationRequestDTO companyRequest = new NotificationRequestDTO();
        companyRequest.setType(NotificationType.CONVENTION_VALIDATION);
        companyRequest.setChannel(NotificationChannel.IN_APP);
        companyRequest.setSenderId(sender.getId());
        companyRequest.setSubject("Décision administrative sur votre convention");
        companyRequest.setContent("Votre convention a été " + 
                (isApproved ? "approuvée" : "rejetée") + 
                (isApproved ? "" : ". Raison : " + convention.getRejectionReason()));
        companyRequest.setUserIds(Collections.singleton(convention.getCompany().getId()));
        
        notificationService.sendNotification(companyRequest);
        
        // Notify student only if approved
        if (isApproved) {
            NotificationRequestDTO studentRequest = new NotificationRequestDTO();
            studentRequest.setType(NotificationType.CONVENTION_VALIDATION);
            studentRequest.setChannel(NotificationChannel.IN_APP);
            studentRequest.setSenderId(sender.getId());
            studentRequest.setSubject("Votre convention de stage a été approuvée");
            studentRequest.setContent("Félicitations ! Votre convention de stage pour l'offre \"" + 
                    convention.getInternshipOffer().getTitle() + "\" a été approuvée.");
            studentRequest.setUserIds(Collections.singleton(convention.getStudent().getId()));
            
            notificationService.sendNotification(studentRequest);
        }
    }
}