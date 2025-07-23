package com.reliance.grievance.controller;

import com.reliance.grievance.dto.ConversationDTO;
import com.reliance.grievance.entity.ConcernedAuthorityMaster;
import com.reliance.grievance.entity.Conversation;
import com.reliance.grievance.entity.Grievance;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.helper.MailHelper;
import com.reliance.grievance.repository.ConcernedAuthorityRepository;
import com.reliance.grievance.repository.ConversationRepository;
import com.reliance.grievance.repository.GrievanceRepository;
import com.reliance.grievance.service.GrievanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    @Autowired
    private ConversationRepository conversationRepo;

    @Autowired
    private GrievanceRepository grievanceRepo;

    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private ConcernedAuthorityRepository concernedAuthorityRepository;

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private MailHelper mailHelper;

    @GetMapping("/{id}")
    public List<ConversationDTO> getConversation(@PathVariable Integer id) {
        return conversationRepo.findByGrievanceIdOrderBySentOnAsc(id)
                .stream()
                .map(c -> new ConversationDTO(
                        c.getSenderType(),
                        c.getSenderId(),
                        c.getMessage(),
                        c.getSentOn()
                ))
                .toList();
    }


    @PostMapping("/{id}")
    public ResponseEntity<Void> postMessage(@PathVariable Integer id, @RequestBody ConversationDTO dto) {
        Grievance grievance = grievanceRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        grievance.setUpdateDate(LocalDateTime.now());
        grievanceRepo.save(grievance);

        Conversation conversation = new Conversation();
        conversation.setGrievance(grievance);
        conversation.setSenderType(dto.getSenderType());
        conversation.setSenderId(dto.getSenderId());
        conversation.setMessage(dto.getMessage());
        conversation.setSentOn(LocalDateTime.now());

        ConcernedAuthorityMaster authority = grievance.getConcernedAuthority();
        if("AUTHORITY".equalsIgnoreCase(dto.getSenderType()))
            conversation.setSenderType(authority.getLevel());

        conversationRepo.save(conversation);

        // Determine mail recipient
        String subject = "Response Added - Grievance ID: " + grievance.getId();

        StringBuilder sb = new StringBuilder();
        sb.append("<p>A response has been added by <strong>")
                .append(dto.getSenderType())
                .append("</strong> for Grievance ID <strong>")
                .append(grievance.getId())
                .append("</strong>.</p>");

        if ("AUTHORITY".equalsIgnoreCase(dto.getSenderType())) {
            sb.append("<p><strong>Note:</strong> If you do not respond within two weeks, the grievance will be automatically marked as resolved.</p>");
        }

        sb.append("<p>Please log in to the Grievance Redressal Portal to view the full conversation.</p>")
                .append("<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>");

        String messageBody = sb.toString();

        if ("USER".equalsIgnoreCase(dto.getSenderType())) {
            // Mail to assigned authority
//            ConcernedAuthorityMaster authority = grievance.getConcernedAuthority();

            if (authority != null && authority.getEmail() != null && !authority.getEmail().isEmpty()) {
                boolean mailFlag = mailHelper.sendMail(
                        authority.getEmail(),
                        authority.getName(),
                        null,
                        "GrievanceAdmin@reliancegroupindia.com",
                        subject,
                        messageBody
                );
            }
        } else if ("AUTHORITY".equalsIgnoreCase(dto.getSenderType())) {
            String userEmail = grievance.getUserId();
            if (userEmail != null && !userEmail.contains("@")) {
                userEmail = generateEmail(userEmail);
            }
            if (userEmail != null && !userEmail.isEmpty()) {
                boolean mailFlag = mailHelper.sendMail(
                        userEmail,
                        dto.getSenderId(),
                        null,
                        "GrievanceAdmin@reliancegroupindia.com",
                        subject,
                        messageBody
                );
            }
        }

        return ResponseEntity.ok().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> markResolved(@PathVariable Integer id) {
        Grievance grievance = grievanceRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));
        grievanceService.markAsResolved(id);
        String userName = grievance.getUserId();
        String userEmail = "";
        if (userName != null && !userName.isEmpty()) {
            if(!userName.contains("@"))
                userEmail = generateEmail(userEmail);
            String subject = "[Grievance Resolved] - " + grievance.getSubject();
            String text = "<p>Dear User,</p>"
                    + "<p>Your grievance has been marked as <strong>Resolved</strong>. Please find the details below:</p>"
                    + "<ul>"
                    + "<li><strong>Subject:</strong> " + grievance.getSubject() + "</li>"
                    + "<li><strong>Category ID:</strong> " + grievance.getCategoryId() + "</li>"
                    + "<li><strong>Sub-Category ID:</strong> " + grievance.getSubcategoryId() + "</li>"
                    + "<li><strong>Submitted On:</strong> " + grievance.getSubmittedOn() + "</li>"
                    + "<li><strong>Status:</strong> RESOLVED</li>"
                    + "</ul>"
                    + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                    + "<p>Thank you for using the Grievance Redressal Portal.</p>"
                    + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

            boolean mailFlag = mailHelper.sendMail(
                    userEmail,
                    userName,
                    null,
                    "GrievanceAdmin@reliancegroupindia.com",
                    subject,
                    text
            );
        }
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/escalate")
    public ResponseEntity<?> escalateGrievance(@PathVariable Long id) {
        Optional<Grievance> grievanceOpt = grievanceRepository.findById(id);
        if (grievanceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Grievance not found."));        }

        Grievance grievance = grievanceOpt.get();

        // Step 1: Determine next level
        String nextLevel = switch (grievance.getStatus()) {
            case PENDING_AT_CONCERNED_TEAM, L1_ESCALATION -> "L2";
            case L2_ESCALATION -> "L3";
            default -> null;
        };

        if (nextLevel == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Already at highest escalation level."));        }

        // Step 2: Fetch authority for next level
        Optional<ConcernedAuthorityMaster> nextAuthorityOpt =
                concernedAuthorityRepository.findByLocationIdAndCategoryIdAndSubcategoryIdAndLevel(
                        grievance.getLocationId(), grievance.getCategoryId(),grievance.getSubcategoryId(), nextLevel);

        if (nextAuthorityOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No authority found for level " + nextLevel + " with given location and category."));
        }

        ConcernedAuthorityMaster nextAuthority = nextAuthorityOpt.get();

        // Step 3: Update grievance with new authority
        grievance.setStatus(GrievanceStatus.valueOf(nextLevel + "_ESCALATION"));
        grievance.setConcernedAuthority(nextAuthority);
        grievance.setConcernedPersonEmail(nextAuthority.getEmail());
        grievance.setConcernedPersonName(nextAuthority.getName());
        grievance.setConcernedPersonEmpId(nextAuthority.getEmployeeId());
        grievanceRepository.save(grievance);

        try {
            if (!nextAuthority.getEmail().isEmpty()) {
                String subject = "[Grievance Escalated] - " + grievance.getSubject();
                String text = "<p>Dear " + nextAuthority.getName() + ",</p>"
                        + "<p>A grievance has been escalated and assigned to you. Please find the details below:</p>"
                        + "<ul>"
                        + "<li><strong>Grievance ID:</strong> " + grievance.getId() + "</li>"
                        + "<li><strong>Subject:</strong> " + grievance.getSubject() + "</li>"
                        + "<li><strong>Category ID:</strong> " + grievance.getCategoryId() + "</li>"
                        + "<li><strong>Sub-Category ID:</strong> " + grievance.getSubcategoryId() + "</li>"
                        + "<li><strong>Submitted On:</strong> " + grievance.getSubmittedOn() + "</li>"
                        + "<li><strong>Submitted By:</strong> " + grievance.getUserId() + "</li>"
                        + "<li><strong>Current Level:</strong> " + grievance.getStatus().name() + "</li>"
                        + "</ul>"
                        + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                        + "<p>Please log in to the Grievance Redressal Portal to take necessary action.</p>"
                        + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

                boolean mailFlag = mailHelper.sendMail(
                        nextAuthority.getEmail(),
                        nextAuthority.getName(),
                        null,
                        "GrievanceAdmin@reliancegroupindia.com",
                        subject,
                        text
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of("message", "Grievance escalated successfully to "+nextLevel));
    }

    public String generateEmail(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        String[] parts = fullName.trim().toLowerCase().split("\\s+");

        String joined = String.join(".", parts);

        return joined + "@reliancegroupindia.com";
    }




}
