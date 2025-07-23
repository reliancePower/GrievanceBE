package com.reliance.grievance.dto;

import com.reliance.grievance.entity.Grievance;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrievanceResponseDTO {
    private Long id;
    private String subject;
    private String details;
    private String category;
    private String subcategory;
    private String status;
    private String userId;
    private String anonymous;
    private LocalDateTime submittedOn;

    public GrievanceResponseDTO(Grievance grievance) {
        this.id = grievance.getId();
        this.subject = grievance.getSubject();
        this.details = grievance.getDetails();
        this.status = String.valueOf(grievance.getStatus());
        this.submittedOn = grievance.getSubmittedOn();
        this.userId = grievance.getUserId();
        this.anonymous = grievance.getAnonymous();
        this.category = grievance.getCategory() != null ? grievance.getCategory().getName() : "";
        this.subcategory = grievance.getSubcategory() != null ? grievance.getSubcategory().getName() : "";
    }

}

