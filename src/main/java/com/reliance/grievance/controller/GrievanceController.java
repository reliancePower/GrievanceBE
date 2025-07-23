package com.reliance.grievance.controller;

import com.reliance.grievance.dto.AttachmentDTO;
import com.reliance.grievance.dto.GrievanceRequestDTO;
import com.reliance.grievance.dto.GrievanceResponseDTO;
import com.reliance.grievance.entity.ConcernedAuthorityMaster;
import com.reliance.grievance.entity.Grievance;
import com.reliance.grievance.entity.GrievanceAttachment;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.helper.MailHelper;
import com.reliance.grievance.repository.ConcernedAuthorityRepository;
import com.reliance.grievance.repository.GrievanceAttachmentRepository;
import com.reliance.grievance.repository.GrievanceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/concerns")
public class GrievanceController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GrievanceController.class);


    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private ConcernedAuthorityRepository concernedAuthorityRepo;

    @Autowired
    MailHelper mailHelper;

    @Autowired
    private GrievanceAttachmentRepository grievanceAttachmentRepo;


    @PostMapping(value = "/submit", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> submitConcern(
            @RequestPart("data") GrievanceRequestDTO dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            HttpServletRequest request){
        try {
            log.info("GrievanceController() : submitConcern()");

            ConcernedAuthorityMaster authority = concernedAuthorityRepo
                    .findByLocationIdAndCategoryIdAndSubcategoryIdAndLevel(dto.getLocationId(), dto.getCategoryId(), dto.getSubcategoryId(), "L1")
                    .orElseThrow(() -> new RuntimeException("No concerned authority found for this location and category"));


            Grievance grievance = new Grievance();
            grievance.setLevelId(dto.getLevelId());
            grievance.setLocationId(dto.getLocationId());
            grievance.setCurrentLocation(dto.getCurrentLocation());
            grievance.setSubject(dto.getSubject());
            grievance.setCategoryId(dto.getCategoryId());
            grievance.setSubcategoryId(dto.getSubcategoryId());
            grievance.setDetails(dto.getDetails());
            grievance.setSubmittedOn(LocalDateTime.now());
            grievance.setAnonymous(dto.getAnonymous());

            grievance.setUserId(dto.getUserId());
            grievance.setUserType(dto.getType());
            grievance.setStatus(GrievanceStatus.PENDING_AT_CONCERNED_TEAM);
            grievance.setConcernedAuthority(authority);
            grievance.setConcernedPersonEmail(authority.getEmail());
            grievance.setConcernedPersonEmpId(authority.getEmployeeId());
            grievance.setConcernedPersonName(authority.getName());

            String userEmail = dto.getEmail();
            //Mail to User
            if (dto.getEmail() != null && !dto
                    .getEmail().isEmpty()) {
                if(!(dto.getEmail()).contains("@"))

                     userEmail = generateEmail(dto.getEmail());
                String userName = dto.getEmail();

                String subject = "[Grievance Submitted] - " + dto.getSubject();
                String text = "<p>Dear " + userName + ",</p>"
                        + "<p>Your grievance has been successfully submitted. Please find the details below:</p>"
                        + "<ul>"
                        + "<li><strong>Subject:</strong> " + dto.getSubject() + "</li>"
                        + "<li><strong>Category:</strong> " + dto.getCategoryId() + "</li>"
                        + "<li><strong>Sub-Category:</strong> " + dto.getSubcategoryId() + "</li>"
                        + "<li><strong>Submitted On:</strong> " + grievance.getSubmittedOn() + "</li>"
                        + "<li><strong>Status:</strong> PENDING_AT_CONCERNED_TEAM</li>"
                        + "</ul>"
                        + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                        + "<p>You will be notified once the grievance is addressed by the concerned authority.</p>"
                        + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

                mailHelper.sendMail(userEmail, userName, null, "GrievanceAdmin@reliancegroupindia.com", subject, text);
            }


            //Mail to concern team
            if(!authority.getEmail().isEmpty()){
                String subject = "[Grievance Assigned] - "+dto.getSubject();
                String text = "<p>Dear " + authority.getName() + ",</p>"
                        + "<p>A new grievance has been assigned to you. Please find the details below:</p>"
                        + "<ul>"
                        + "<li><strong>Subject:</strong> " + dto.getSubject() + "</li>"
                        + "<li><strong>Category ID:</strong> " + dto.getCategoryId() + "</li>"
                        + "<li><strong>Sub-Category ID:</strong> " + dto.getSubcategoryId() + "</li>"
                        + "<li><strong>Submitted On:</strong> " + grievance.getSubmittedOn() + "</li>"
                        + "<li><strong>Submitted By:</strong> " + grievance.getUserId() + "</li>"
                        + "</ul>"
                        + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                        + "<p>Please log in to the Grievance Redressal Portal to respond.</p>"
                        + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

                String cc = null;
                if (!"Y".equalsIgnoreCase(grievance.getAnonymous())) {
                    cc = grievance.getUserId();
                    if (cc != null && !cc.contains("@")) {
                        cc = generateEmail(cc);
                    }
                }
                boolean mailFlag = mailHelper.sendMailCc(authority.getEmail(),authority.getName(), null,"GrievanceAdmin@reliancegroupindia.com",subject,text,cc);

            }


            String clientIp = request.getHeader("X-FORWARDED-FOR");
            if (clientIp == null) {
                clientIp = request.getRemoteAddr();
            }
            grievance.setHostname(clientIp);

            grievanceRepository.save(grievance);

            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    GrievanceAttachment attachment = new GrievanceAttachment();
                    attachment.setGrievance(grievance);
                    attachment.setFileName(file.getOriginalFilename());
                    attachment.setFileType(file.getContentType());
                    attachment.setData(file.getBytes());
                    grievanceAttachmentRepo.save(attachment);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Grievance submitted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit Grievance. Please try again later.");
        }
    }

    @GetMapping("/my-concerns/{userId}")
    public ResponseEntity<?> getConcernsByUser(@PathVariable String userId) {
        try {
            log.info("GrievanceController() : getConcernsByUser()");
            List<Grievance> concerns = grievanceRepository.findByUserIdIgnoreCase(userId);

            List<GrievanceResponseDTO> response = concerns.stream()
                    .map(GrievanceResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch Grievance.");
        }
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedGrievances(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String employeeId) {

        try {
            log.info("GrievanceController() : getAssignedGrievances()");
            log.info("Employee ID : {} Name : {}", employeeId, name);

            List<Grievance> grievances;

            if (employeeId != null && !employeeId.isBlank()) {
                grievances = grievanceRepository.findByConcernedPersonEmpId(employeeId);
            } else if (name != null && !name.isBlank()) {
                grievances = grievanceRepository.findByConcernedPersonName(name);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either name or employeeId must be provided.");
            }

            log.info("Assigned Grievances Found: {}", grievances.size());

            List<GrievanceResponseDTO> response = grievances.stream()
                    .map(GrievanceResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch Grievance.");
        }
    }


    @GetMapping("/{id}/attachments")
    public List<AttachmentDTO> getAttachments(@PathVariable Integer id) {
        return grievanceAttachmentRepo.findByGrievanceId(id).stream()
                .map(a -> new AttachmentDTO(a.getId(), a.getFileName(), a.getFileType(), Base64.getEncoder().encodeToString(a.getData())))
                .collect(Collectors.toList());
    }

    @GetMapping("/is-authority/{email}")
    public ResponseEntity<Boolean> isUserInAuthority(@PathVariable String email) {
        if(!email.contains("@"))
         email = generateEmail(email);
        boolean exists = concernedAuthorityRepo.existsByEmailIgnoreCase(email);
        return ResponseEntity.ok(exists);
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

