package com.reliance.grievance.util;

import com.reliance.grievance.config.AppConfig;
import com.reliance.grievance.controller.GrievanceController;
import com.reliance.grievance.entity.Grievance;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.helper.MailHelper;
import com.reliance.grievance.repository.ConcernedAuthorityRepository;
import com.reliance.grievance.repository.GrievanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EscalationService {

    private final GrievanceRepository grievanceRepo;
    private final ConcernedAuthorityRepository authorityRepo;
    private final MailHelper mailHelper;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrievanceController.class);


    @Autowired
    AppConfig appConfig;

    // Run daily at 2:05 AM
    @Scheduled(cron = "0 5 2 * * *")
//    @Scheduled(cron = "0 50 14 * * *")
    @Transactional
    public void escalateBySubmissionAge() {
        log.info("Scheduler Run : EscalationService");
        var open = grievanceRepo.findByStatusIn(List.of(GrievanceStatus.WITH_L1,GrievanceStatus.WITH_L2));
        var today = LocalDate.now();

        for (Grievance g : open) {
            if (g.getSubmittedOn() == null) continue;

            long ageDays = ChronoUnit.DAYS.between(g.getSubmittedOn().toLocalDate(), today);
            if (ageDays < 7) continue; // still within L1 window

            if (g.getStatus() == GrievanceStatus.WITH_L1 && ageDays >= 7) {
                escalateTo(g, "L2", GrievanceStatus.WITH_L2);
            } else if (g.getStatus() == GrievanceStatus.WITH_L2 && ageDays >= 14) {
                escalateTo(g, "L3", GrievanceStatus.WITH_L3);
            }
        }
    }

    private void escalateTo(Grievance g, String level, GrievanceStatus targetStatus) {
        var nextOpt = authorityRepo.findByLocationIdAndCategoryIdAndSubcategoryIdAndLevel(
                g.getLocationId(), g.getCategoryId(), g.getSubcategoryId(), level);

        if (nextOpt.isEmpty()) return; // or alert admins

        var next = nextOpt.get();
        g.setConcernedAuthority(next);
        g.setConcernedPersonEmail(next.getEmail());
        g.setConcernedPersonEmpId(next.getEmployeeId());
        g.setConcernedPersonName(next.getName());
        g.setStatus(targetStatus);
        grievanceRepo.save(g);

        String subject = "[Grievance Escalated] - ID: " + g.getId();
        String body = """
      <p>Dear  %s,</p>
      <p>This grievance has been automatically escalated to you based on age from submission.</p>
      <ul>
        <li><strong>ID:</strong> %s</li>
        <li><strong>Subject:</strong> %s</li>
        <li><strong>Current Level:</strong> %s</li>
      </ul>
      <p><a href="%s" target="_blank">Open in Portal</a></p>
      <p>Regards,<br/>Grievance Redressal System</p>
      """.formatted(next.getName(), g.getId(), safe(g.getSubject()), level, appConfig.getPortalUrl());

        mailHelper.sendMail(next.getEmail(), next.getName(), null,
                "GrievanceAdmin@reliancegroupindia.com", subject, body);
    }

    private String safe(String s){ return s==null?"":s; }
}

