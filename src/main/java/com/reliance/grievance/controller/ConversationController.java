package com.reliance.grievance.controller;

import com.reliance.grievance.config.AppConfig;
import com.reliance.grievance.dto.ConcernedAuthorityDTO;
import com.reliance.grievance.dto.ConversationDTO;
import com.reliance.grievance.dto.GrievanceResponseDTO;
import com.reliance.grievance.entity.*;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.helper.MailHelper;
import com.reliance.grievance.repository.*;
import com.reliance.grievance.service.GrievanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;


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

    @Autowired
    private CategoryMasterRepository categoryRepo;

    @Autowired
    private SubCategoryMasterRepository subCategoryRepo;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private GrievanceAssignmentRepo grievanceAssignmentRepo;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrievanceController.class);


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

        if (grievance.getStatus() == GrievanceStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This grievance is already resolved. Messaging is closed.");
        }

        grievance.setUpdateDate(LocalDateTime.now());
        grievanceRepo.save(grievance);

        Conversation conversation = new Conversation();
        conversation.setGrievance(grievance);
        conversation.setSenderType(dto.getSenderType());
        conversation.setSenderId(dto.getSenderId());
        conversation.setMessage(dto.getMessage());
        conversation.setSentOn(LocalDateTime.now());

        String type="";

        ConcernedAuthorityMaster authority = grievance.getConcernedAuthority();
        if("AUTHORITY".equalsIgnoreCase(dto.getSenderType())){
            conversation.setSenderType(authority.getLevel());
            type = authority.getLevel();
        }
        else{
            if (grievance.getAnonymous().equalsIgnoreCase("Y"))
                type="Anonymous";
            else
                type=grievance.getUserId();
        }
        conversationRepo.save(conversation);

        // Determine mail recipient
        String subject = "Response Added - Issue/Suggestion ID: " + grievance.getExternalId();

        StringBuilder sb = new StringBuilder();
        sb.append("<p>A new response has been added by <strong>")
                .append(dto.getSenderType())
                .append(" - ")
                .append(type)
                .append("for the")
                .append("</strong>Issue/Suggestion ID: <strong>")
                .append(grievance.getExternalId())
                .append("</strong>.</p>");

        if ("AUTHORITY".equalsIgnoreCase(dto.getSenderType())) {
            sb.append("<p><strong>Note:</strong> If you do not respond within two weeks, the Issue/Suggestion will be automatically marked as resolved.</p>");
        }

        sb.append("<p>You may log in to the Employee Support & Resolution Portal to view the details.</p>")
        .append("<p><a href=\"").append(appConfig.getPortalUrl()).append("\" target=\"_blank\">Click here to access the Portal</a></p>")

                .append("<p>Warm Regards,<br/><strong>Employee Support & Resolution Portal</strong><br/>Reliance Power Limited</p>");

        String messageBody = sb.toString();

        if ("USER".equalsIgnoreCase(dto.getSenderType())) {

            if (authority != null && authority.getEmail() != null && !authority.getEmail().isEmpty()) {
                boolean mailFlag = mailHelper.sendMail(
                        authority.getEmail(),
                        authority.getName(),
                        null,
                        "Resolve360Admin@reliancegroupindia.com",
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
                        "Resolve360Admin@reliancegroupindia.com",
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
        String authorityName = grievance.getConcernedPersonName();
        String authorityEmail = grievance.getConcernedPersonEmail();
        String categoryName = categoryRepo.findById(Long.valueOf(grievance.getCategoryId()))
                .map(CategoryMaster::getName)
                .orElse("Unknown Category");

        String subCategoryName = subCategoryRepo.findById(Long.valueOf(grievance.getSubcategoryId()))
                .map(SubCategoryMaster::getName)
                .orElse("Unknown Sub-Category");

        String formattedDate = grievance.getSubmittedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        if (userName != null && !userName.isEmpty()) {
            if(!userName.contains("@"))
                userEmail = generateEmail(userName);
            String subject = "[Issue/Suggestion Resolved] - " + grievance.getSubject();
            String text = "<p>Dear " + userName + ",</p>"
                    + "<p>Your Issue/Suggestion has been <strong>Resolved</strong>. Please find the details below:</p>"
                    + "<ul>"
                    + "<li><strong>Subject:</strong> " + grievance.getSubject() + "</li>"
                    + "<li><strong>Category:</strong> " + categoryName + "</li>"
                    + "<li><strong>Sub-Category:</strong> " + subCategoryName + "</li>"
                    + "<li><strong>Submitted On:</strong> " + formattedDate+ "</li>"
                    + "<li><strong>Status:</strong> RESOLVED</li>"
                    + "</ul>"
                    + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                    + "<p><a href=\"" + appConfig.getPortalUrl() + "\" target=\"_blank\">Click here to access the Portal</a></p>"
                    + "<p>Thank you for using the Employee Support & Resolution Portal.</p>"
                    + "<p>Warm Regards,<br/><strong>Employee Support & Resolution Portal</strong><br/>Reliance Power Limited</p>";

            boolean mailFlag = mailHelper.sendMailCc(
                    userEmail,
                    userName,
                    null,
                    "Resolve360Admin@reliancegroupindia.com",
                    subject,
                    text,authorityEmail
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

        String categoryName = categoryRepo.findById(Long.valueOf(grievance.getCategoryId()))
                .map(CategoryMaster::getName)
                .orElse("Unknown Category");

        String subCategoryName = subCategoryRepo.findById(Long.valueOf(grievance.getSubcategoryId()))
                .map(SubCategoryMaster::getName)
                .orElse("Unknown Sub-Category");

        // Step 1: Determine next level
        String nextLevel = switch (grievance.getStatus()) {
            case WITH_L1 -> "L2";
            case WITH_L2 -> "L3";
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
        grievance.setStatus(GrievanceStatus.valueOf("WITH_"+nextLevel));
        grievance.setConcernedAuthority(nextAuthority);
        grievance.setConcernedPersonEmail(nextAuthority.getEmail());
        grievance.setConcernedPersonName(nextAuthority.getName());
        grievance.setConcernedPersonEmpId(nextAuthority.getEmployeeId());
        grievanceRepository.save(grievance);

        grievanceAssignmentRepo.findByGrievanceIdAndActiveTrue(id).ifPresent(cur -> {
            cur.setActive(false);
            cur.setUnassignedOn(LocalDateTime.now());
            grievanceAssignmentRepo.save(cur);
        });

        GrievanceAssignment next = new GrievanceAssignment();
        next.setGrievance(grievance);
        next.setAuthority(nextAuthority);
        next.setLevel(nextAuthority.getLevel());
        next.setAssignedOn(LocalDateTime.now());
        next.setActive(true);
        grievanceAssignmentRepo.save(next);

        String formattedDate = grievance.getSubmittedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        try {
            if (!nextAuthority.getEmail().isEmpty()) {
                String userName = "";
                if(grievance.getAnonymous().equalsIgnoreCase("Y"))
                    userName="Anonymous";
                else
                    userName=grievance.getUserId();

                String subject = "[Grievance Escalated] - " + grievance.getSubject();
                String text = "<p>Dear " + nextAuthority.getName() + ",</p>"
                        + "<p>A grievance has been escalated and assigned to you. Please find the details below:</p>"
                        + "<ul>"
                        + "<li><strong>Grievance ID:</strong> " + grievance.getExternalId() + "</li>"
                        + "<li><strong>Subject:</strong> " + grievance.getSubject() + "</li>"
                        + "<li><strong>Category ID:</strong> " + categoryName + "</li>"
                        + "<li><strong>Sub-Category ID:</strong> " + subCategoryName + "</li>"
                        + "<li><strong>Submitted On:</strong> " + formattedDate + "</li>"
                        + "<li><strong>Submitted By:</strong> " + userName + "</li>"
                        + "<li><strong>Current Level:</strong> " + grievance.getStatus().name() + "</li>"
                        + "</ul>"
                        + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                        + "<p>Please log in to the Grievance Redressal Portal to take necessary action.</p>"
                        + "<p><a href=\"" + appConfig.getPortalUrl() + "\" target=\"_blank\">Click here to access the Portal</a></p>"
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

    @GetMapping("/{id}/levels")
    public ResponseEntity<List<ConcernedAuthorityDTO>> getEscalationLevels(@PathVariable Long id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        List<ConcernedAuthorityMaster> authorities = concernedAuthorityRepository.findByLocationIdAndCategoryIdAndSubcategoryIdOrderByLevelAsc(
                grievance.getLocationId(),
                grievance.getCategoryId(),
                grievance.getSubcategoryId()
        );

        List<ConcernedAuthorityDTO> response = authorities.stream()
                .map(ConcernedAuthorityDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/rate")
    public ResponseEntity<Map<String, String>> submitRating(@PathVariable Long id, @RequestParam Integer rating) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        grievance.setUserRating(rating);
        grievance.setUserRatingSubmitted(true);
        grievanceRepository.save(grievance);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Rating submitted successfully.");

        return ResponseEntity.ok(response);
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }


    @GetMapping("/{id}/getDetails")
    public ResponseEntity<GrievanceResponseDTO> getDetails(@PathVariable Long id) {
        Grievance grievance = grievanceRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        return ResponseEntity.ok(new GrievanceResponseDTO(grievance));
    }







}
