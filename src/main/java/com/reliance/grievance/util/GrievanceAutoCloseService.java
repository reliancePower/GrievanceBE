package com.reliance.grievance.util;

import com.reliance.grievance.config.AppConfig;
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

    @Autowired
    private AppConfig appConfig;

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
                String authEmail = grievance.getConcernedPersonEmail();
                String userName = grievance.getUserId();
                if (userEmail != null && !userEmail.contains("@")) {
                    userEmail = generateEmail(userEmail);
                }

                String subject = "Inactivity related to Issue/Suggestion  - ID: " + grievance.getExternalId();
                String message = "<p>Dear "+userName+",</p>"
                        + "<p>There was no response in the past 14 days from you, related to the Issue/Suggestion ID: "+grievance.getExternalId()+"</p>"
                        + "<p>If the issue still persists, we kindly request you to raise a new Issue/Suggestion through the portal.</p>"
                        + "<p><a href=\"" + appConfig.getPortalUrl() + "\" target=\"_blank\">Click here to access the Portal</a></p>"
                        + "<p>Warm Regards,<br/><strong>Employee Support & Resolution Portal</strong><br/>Reliance Power Limited</p>";

                if (userEmail != null && !userEmail.isEmpty()) {
                    mailHelper.sendMail(
                            userEmail,
                            grievance.getUserId(),
                            null,
                            "Resolve360Admin@reliancegroupindia.com",
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

