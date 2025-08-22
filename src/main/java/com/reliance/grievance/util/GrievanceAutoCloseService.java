package com.reliance.grievance.util;

import com.reliance.grievance.controller.GrievanceController;
import com.reliance.grievance.entity.Grievance;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.helper.MailHelper;
import com.reliance.grievance.repository.GrievanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GrievanceAutoCloseService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrievanceController.class);


    @Autowired
    private GrievanceRepository grievanceRepo;

    @Autowired
    private MailHelper mailHelper;

    @Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2 AM
    public void autoCloseInactiveGrievances() {

        log.info("Scheduler Run : EscalationService");

        List<Grievance> grievances = grievanceRepo.findAllByStatusNot(GrievanceStatus.RESOLVED);

        for (Grievance grievance : grievances) {
            if (grievance.getUpdateDate() == null) continue;

            long daysSinceUpdate = Duration.between(grievance.getUpdateDate(), LocalDateTime.now()).toDays();
            if (daysSinceUpdate >= 14) {
                grievance.setStatus(GrievanceStatus.RESOLVED);
                grievance.setUpdateDate(LocalDateTime.now()); // optionally track resolution date
                grievanceRepo.save(grievance);

                // Send mail
                String userEmail = grievance.getUserId();
                if (userEmail != null && !userEmail.contains("@")) {
                    userEmail = generateEmail(userEmail);
                }

                String subject = "Grievance Closed Due to Inactivity - ID: " + grievance.getId();
                String message = "<p>Dear User,</p>"
                        + "<p>Your grievance (ID: <strong>" + grievance.getId() + "</strong>) has been automatically marked as <strong>Resolved</strong> due to no activity in the last 14 days.</p>"
                        + "<p>If the issue still persists, you may raise a new grievance.</p>"
                        + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

                if (userEmail != null && !userEmail.isEmpty()) {
                    mailHelper.sendMail(
                            userEmail,
                            grievance.getUserId(),
                            null,
                            "GrievanceAdmin@reliancegroupindia.com",
                            subject,
                            message
                    );
                }
            }
        }
    }

    private String generateEmail(String id) {
        return id + "@reliancegroupindia.com";
    }
}

