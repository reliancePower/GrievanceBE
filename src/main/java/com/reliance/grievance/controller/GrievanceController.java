package com.reliance.grievance.controller;

import com.reliance.grievance.config.AppConfig;
import com.reliance.grievance.dto.*;
import com.reliance.grievance.entity.*;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.helper.MailHelper;
import com.reliance.grievance.repository.*;
import com.reliance.grievance.service.UserDirectoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;


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

    @Autowired
    private CategoryMasterRepository categoryRepo;

    @Autowired
    private SubCategoryMasterRepository subCategoryRepo;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private GrievanceAssignmentRepo grievanceAssignmentRepo;

    @Autowired
    UserDirectoryService userDirectoryService;



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

            String formattedUserId = capitalizeWords(dto.getUserId());
            grievance.setUserId(formattedUserId);
            grievance.setUserType(dto.getType());
            grievance.setStatus(GrievanceStatus.WITH_L1);
            grievance.setConcernedAuthority(authority);
            grievance.setConcernedPersonEmail(authority.getEmail());
            grievance.setConcernedPersonEmpId(authority.getEmployeeId());
            grievance.setConcernedPersonName(authority.getName());

            String hrDbEmail = dto.getUserId();
            if (hrDbEmail != null && !hrDbEmail.isBlank() && !hrDbEmail.contains("@")) {
                hrDbEmail = generateEmail(hrDbEmail);
            }

//            String mobile = userDirectoryService.findMobileByEmail(hrDbEmail).orElse(null);
//            if (mobile == null && dto.getMobile() != null && !dto.getMobile().isBlank()) {
//                grievance.setUserMobile(dto.getMobile());
//            }
//            else
//                grievance.setUserMobile(mobile);
//
//            String prNo   = userDirectoryService.findPrNoByEmail(hrDbEmail).orElse(null);
//            if (prNo == null && dto.getEmpId() != null && !dto.getEmpId().isBlank()) {
//                grievance.setEmpId(dto.getEmpId());
//            } else {
//                grievance.setEmpId(prNo);
//            }

            var hr = userDirectoryService.findHrInfoByEmail(hrDbEmail).orElse(null);

            if (hr != null) {
                String mobile = hr.getMobile();
                if ((mobile == null || mobile.isBlank()) && dto.getMobile() != null && !dto.getMobile().isBlank()) {
                    grievance.setUserMobile(dto.getMobile());
                } else {
                    grievance.setUserMobile(mobile);
                }

                grievance.setEmpId(hr.getPrNo());
                grievance.setCurrentLocation(hr.getLocation());
                grievance.setUserDept(hr.getDept());
            }


            String clientIp = request.getHeader("X-FORWARDED-FOR");
            if (clientIp == null) {
                clientIp = request.getRemoteAddr();
            }
            grievance.setHostname(clientIp);
            grievanceRepository.save(grievance);

            GrievanceAssignment ga = new GrievanceAssignment();
            ga.setGrievance(grievance);
            ga.setAuthority(authority);
            ga.setLevel(authority.getLevel());
            ga.setAssignedOn(LocalDateTime.now());
            ga.setActive(true);
            grievanceAssignmentRepo.save(ga);

            Long grievanceId = grievance.getId();
            String ticket = grievance.getExternalId();
            String formattedDate = grievance.getSubmittedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));


            String categoryName = categoryRepo.findById(Long.valueOf(dto.getCategoryId()))
                    .map(CategoryMaster::getName)
                    .orElse("Unknown Category");

            String subCategoryName = subCategoryRepo.findById(Long.valueOf(dto.getSubcategoryId()))
                    .map(SubCategoryMaster::getName)
                    .orElse("Unknown Sub-Category");

            String userEmail = dto.getEmail();
            if (dto.getEmail() != null && !dto
                    .getEmail().isEmpty()) {
                if(!(dto.getEmail()).contains("@"))

                     userEmail = generateEmail(dto.getEmail());
                String userName = dto.getEmail();

                String subject = "[Grievance Submitted: #" + grievanceId + "] - " + dto.getSubject();
                String text = "<p>Dear " + userName + ",</p>"
                        + "<p>Your grievance has been successfully submitted. Please find the details below:</p>"
                        + "<ul>"
                        + "<li><strong>Grievance ID:</strong> " + grievanceId + "</li>"
                        + "<li><strong>Subject:</strong> " + dto.getSubject() + "</li>"
                        + "<li><strong>Category:</strong> " + categoryName + "</li>"
                        + "<li><strong>Sub-Category:</strong> " + subCategoryName + "</li>"
                        + "<li><strong>Submitted On:</strong> " + formattedDate + "</li>"
                        + "<li><strong>Status:</strong> WITH_L1</li>"
                        + "</ul>"
                        + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                        + "<p>You will be notified once the grievance is addressed by the concerned authority.</p>"
                        + "<p><a href=\"" + appConfig.getPortalUrl() + "\" target=\"_blank\">Click here to access the Portal</a></p>"
                        + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

                mailHelper.sendMail(userEmail, userName, null, "GrievanceAdmin@reliancegroupindia.com", subject, text);
            }


            //Mail to concern team
            if(!authority.getEmail().isEmpty()){
                String userName="";
                if(grievance.getAnonymous().equalsIgnoreCase("Y"))
                    userName="Anonymous";
                else
                    userName=grievance.getUserId();
                String subject = "[Grievance Assigned: #" + grievanceId + "] - " + dto.getSubject();
                String text = "<p>Dear " + authority.getName() + ",</p>"
                        + "<p>A new grievance has been assigned to you. Please find the details below:</p>"
                        + "<ul>"
                        + "<li><strong>Grievance ID:</strong> " + grievanceId + "</li>"
                        + "<li><strong>Subject:</strong> " + dto.getSubject() + "</li>"
                        + "<li><strong>Category ID:</strong> " + categoryName + "</li>"
                        + "<li><strong>Sub-Category ID:</strong> " + subCategoryName + "</li>"
                        + "<li><strong>Submitted On:</strong> " + formattedDate + "</li>"
                        + "<li><strong>Submitted By:</strong> " + userName + "</li>"
                        + "</ul>"
                        + "<p><strong>Description:</strong><br/>" + grievance.getDetails() + "</p>"
                        + "<p>Please log in to the Grievance Redressal Portal to respond.</p>"
                        + "<p><a href=\"" + appConfig.getPortalUrl() + "\" target=\"_blank\">Click here to access the Portal</a></p>"
                        + "<p>Regards,<br/>Grievance Redressal System<br/>Reliance Power Limited</p>";

//                String cc = null;
//                if (!"Y".equalsIgnoreCase(grievance.getAnonymous())) {
//                    cc = grievance.getUserId();
//                    if (cc != null && !cc.contains("@")) {
//                        cc = generateEmail(cc);
//                    }
//                }
                boolean mailFlag = mailHelper.sendMail(authority.getEmail(),authority.getName(), null,"GrievanceAdmin@reliancegroupindia.com",subject,text);

            }

            if (files != null) {
                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;

                    String contentType = Optional.ofNullable(file.getContentType()).orElse("");
                    if (!ALLOWED_TYPES.contains(contentType)) {
                        // Reject unknown types (or relax this if you prefer)
                        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                "Unsupported file type: " + contentType);
                    }

                    GrievanceAttachment att = new GrievanceAttachment();
                    att.setGrievance(grievance);
                    att.setFileName(file.getOriginalFilename());
                    att.setFileType(contentType);
                    att.setData(file.getBytes());
                    att.setSizeBytes(file.getSize());
                    att.setUploadedOn(LocalDateTime.now());

                    grievanceAttachmentRepo.save(att);
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
            List<Grievance> concerns = grievanceRepository.findByUserIdIgnoreCaseOrderBySubmittedOnDesc(userId);

            List<GrievanceResponseDTO> response = concerns.stream()
                    .map(GrievanceResponseDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch Grievance.");
        }
    }

//    @GetMapping("/assigned")
//    public ResponseEntity<?> getAssignedGrievances(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String employeeId) {
//
//        try {
//            log.info("GrievanceController() : getAssignedGrievances()");
//            log.info("Employee ID : {} Name : {}", employeeId, name);
//
//            List<Grievance> grievances;
//
//            if (employeeId != null && !employeeId.isBlank()) {
//                grievances = grievanceRepository.findByConcernedPersonEmpIdOrderBySubmittedOnDesc(employeeId);
//            } else if (name != null && !name.isBlank()) {
//                grievances = grievanceRepository.findByConcernedPersonName(name);
//            } else {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either name or employeeId must be provided.");
//            }
//
//            log.info("Assigned Grievances Found: {}", grievances.size());
//
//            List<GrievanceResponseDTO> response = grievances.stream()
//                    .map(GrievanceResponseDTO::new)
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to fetch Grievance.");
//        }
//    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedGrievances(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String employeeId) {

        try {
            log.info("GrievanceController() : getAssignedGrievances()");
            log.info("Employee ID : {}  Email(name) : {}", employeeId, name);

            // 1) Resolve which authority records belong to the requester
            List<ConcernedAuthorityMaster> myAuthorityScopes;
            String viewerEmail = null;

            if (employeeId != null && !employeeId.isBlank()) {
                myAuthorityScopes = concernedAuthorityRepo.findByEmployeeId(employeeId);
            } else if (name != null && !name.isBlank()) {
                viewerEmail = name.trim();
                myAuthorityScopes = concernedAuthorityRepo.findByNameIgnoreCase(viewerEmail);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either name(email) or employeeId must be provided.");
            }

            if (myAuthorityScopes.isEmpty()) {
                // Not an authority anywhere → return empty list (they will still see "My Posted" elsewhere)
                return ResponseEntity.ok(Collections.emptyList());
            }

            // 2) For each authority scope (L1/L2/L3 for some loc+cat+subcat), pull grievances in that dimension
            //    and mark canRespond = true only if viewer matches the current assigned person.
            Map<Long, AssignedGrievanceDTO> result = new LinkedHashMap<>();

            for (ConcernedAuthorityMaster scope : myAuthorityScopes) {
                Integer locId = scope.getLocation().getId();
                Integer catId = scope.getCategory().getId();
                Integer subId = scope.getSubcategory() != null ? scope.getSubcategory().getId() : null;

                // If your CAM always has a subcategory, keep as-is; else, you may wish to handle null by skipping or widening.
                if (subId == null) continue;

                List<Grievance> list = grievanceRepository.findByLocationIdAndCategoryIdAndSubcategoryIdOrderBySubmittedOnDesc(locId, catId, subId);

                for (Grievance g : list) {
                    // canRespond only if viewer is the CURRENT assignee
                    boolean canRespond =
                            (employeeId != null && employeeId.equalsIgnoreCase(g.getConcernedPersonEmpId())) ||
                                    (viewerEmail != null && viewerEmail.equalsIgnoreCase(g.getConcernedPersonName()));

//                    log.info("Boolean: "+canRespond);
                    // Viewer level for this grievance is the level from the scope we’re iterating
                    String viewerLevel = scope.getLevel();

                    // If the grievance is already in map (because viewer is L1 & L2 for same dimension),
                    // keep the one that gives canRespond = true, else keep the first.
                    result.merge(
                            g.getId(),
                            new AssignedGrievanceDTO(g, viewerLevel, canRespond),
                            (oldDto, newDto) -> {
                                if (oldDto.isCanRespond()) return oldDto;
                                if (newDto.isCanRespond()) return newDto;
                                return oldDto; // stable
                            }
                    );
                }
            }

            // 3) Return as list ordered by submittedOn desc (already fetched in desc; map preserves insertion order)
            return ResponseEntity.ok(new ArrayList<>(result.values()));

        } catch (Exception e) {
            log.error("Failed to fetch Assigned grievances", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch Grievance.");
        }
    }



    @GetMapping("/{grievanceId}/attachments")
    public ResponseEntity<List<AttachmentDTO>> listAttachments(@PathVariable Long grievanceId) {
        Grievance g = grievanceRepository.findById(grievanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        List<GrievanceAttachment> atts = grievanceAttachmentRepo
                .findByGrievanceIdOrderByUploadedOnDesc(g.getId());

        List<AttachmentDTO> dto = atts.stream()
                .map(a -> {
                    // IMPORTANT: plain Base64 encoder (no line breaks)
                    String base64 = Base64.getEncoder().encodeToString(a.getData());
                    return new AttachmentDTO(
                            a.getId(),
                            a.getFileName(),
                            a.getFileType(),
                            a.getSizeBytes(),
                            a.getUploadedOn(),
                            base64
                    );
                })
                .toList();

        return ResponseEntity.ok(dto);
    }


    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> viewAttachment(@PathVariable Long attachmentId) {
        GrievanceAttachment att = grievanceAttachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        ByteArrayResource resource = new ByteArrayResource(att.getData());
        String contentType = Optional.ofNullable(att.getFileType()).orElse("application/octet-stream");

        // inline for images and pdf; attachment for others (doc/docx will usually download anyway)
        boolean inline = contentType.startsWith("image/") || contentType.equals("application/pdf");
        String dispositionType = inline ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + att.getFileName() + "\"")
                .contentLength(att.getData().length)
                .body(resource);
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

    @GetMapping("/{id}/rating-submitted")
    public ResponseEntity<Boolean> hasRatingBeenSubmitted(@PathVariable Long id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        return ResponseEntity.ok(Boolean.TRUE.equals(grievance.getUserRatingSubmitted()));
    }

    @GetMapping("/{id}/rating")
    public ResponseEntity<Integer> getUserRating(@PathVariable Long id) {
        Grievance grievance = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        // If null (not yet rated), return 0
        return ResponseEntity.ok(grievance.getUserRating() != null ? grievance.getUserRating() : 0);
    }


    @GetMapping("/escalation-levels")
    public ResponseEntity<List<ConcernedAuthorityDTO>> getEscalationLevelsByParams(
            @RequestParam Integer locationId,
            @RequestParam Integer categoryId,
            @RequestParam Integer subcategoryId) {

        List<ConcernedAuthorityMaster> authorities = concernedAuthorityRepo
                .findByLocationIdAndCategoryIdAndSubcategoryIdOrderByLevelAsc(locationId, categoryId, subcategoryId);

        List<ConcernedAuthorityDTO> response = authorities.stream()
                .map(ConcernedAuthorityDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<Map<String, String>> withdraw(@PathVariable Long id) {
        Grievance g = grievanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        if (g.getStatus() == GrievanceStatus.RESOLVED || g.getStatus() == GrievanceStatus.WITHDRAWN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grievance already closed.");
        }

        g.setStatus(GrievanceStatus.WITHDRAWN);
        g.setUpdateDate(LocalDateTime.now());
        grievanceRepository.save(g);

        ConcernedAuthorityMaster assignee = g.getConcernedAuthority();
        if (assignee != null && assignee.getEmail() != null && !assignee.getEmail().isBlank()) {
            String subject = "[Grievance Withdrawn] - ID: " + g.getId();
            String body =
                    "<p>Dear " + assignee.getName() + ",</p>" +
                            "<p>The following grievance has been withdrawn by the user:</p>" +
                            "<ul>" +
                            "<li><strong>ID:</strong> " + g.getId() + "</li>" +
                            "<li><strong>Subject:</strong> " + g.getSubject() + "</li>" +
                            "<li><strong>Status:</strong> WITHDRAWN</li>" +
                            "</ul>" +
                            "<p>Regards,<br/>Grievance Redressal System</p>";
            mailHelper.sendMail(assignee.getEmail(), assignee.getName(), null,
                    "GrievanceAdmin@reliancegroupindia.com", subject, body);
        }

        return ResponseEntity.ok(Map.of("message", "Grievance withdrawn successfully."));
    }

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @GetMapping("/is-super")
    public Map<String, Object> isSuper(@RequestParam("email") String email) {
        if(!email.contains("@"))
            email = generateEmail(email);
        boolean isSuper = concernedAuthorityRepo.findFirstByEmailIgnoreCase(email)
                .map(a -> "Y".equalsIgnoreCase(a.getIsSuper()))
                .orElse(false);
        return Map.of("isSuper", isSuper);
    }

    private String capitalizeWords(String input) {
        if (input == null || input.isBlank()) return input;

        return Arrays.stream(input.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }




}

