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
    private String locationName;
    private String concernedPersonName;
    private String userMobile;
    private String empId;
    private String currentLocation;
    private String userDept;
    private String externalId;

    public GrievanceResponseDTO(Grievance grievance) {
        this.id = grievance.getId();
        this.subject = grievance.getSubject();
        this.details = grievance.getDetails();
        this.status = String.valueOf(grievance.getStatus());
        this.submittedOn = grievance.getSubmittedOn();
        this.userId = grievance.getUserId();
        this.concernedPersonName = grievance.getConcernedPersonName();
        this.anonymous = grievance.getAnonymous();
        this.category = grievance.getCategory() != null ? grievance.getCategory().getName() : "";
        this.subcategory = grievance.getSubcategory() != null ? grievance.getSubcategory().getName() : "";
        this.locationName = grievance.getLocation() != null ? grievance.getLocation().getName() : "";
        this.userMobile = grievance.getUserMobile() != null ? grievance.getUserMobile() : "";
        this.empId = grievance.getEmpId() != null ? grievance.getEmpId() : "";
        this.currentLocation = grievance.getCurrentLocation() != null ? grievance.getCurrentLocation() :"";
        this.userDept = grievance.getUserDept() != null ? grievance.getUserDept() :"";
        this.externalId = grievance.getExternalId() != null ? grievance.getExternalId() :"";
    }

}

