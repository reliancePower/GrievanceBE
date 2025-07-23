package com.reliance.grievance.dto;

import com.reliance.grievance.entity.Grievance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AssignedGrievanceDTO {
    private Long id;
    private String subject;
    private String details;
    private String category;
    private String subcategory;
    private String status;
    private LocalDateTime submittedOn;

    public AssignedGrievanceDTO(Grievance grievance) {
        this.id = grievance.getId();
        this.subject = grievance.getSubject();
        this.details = grievance.getDetails();
        this.status = String.valueOf(grievance.getStatus());
        this.submittedOn = grievance.getSubmittedOn();
        this.category = grievance.getCategory() != null ? grievance.getCategory().getName() : "";
        this.subcategory = grievance.getSubcategory() != null ? grievance.getSubcategory().getName() : "";
    }
}
