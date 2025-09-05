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
    private String level;
    private boolean canRespond;
    private String locationName;
    private String userId;
    private String concernedPersonName;
    private String userMobile;
    private String anonymous;
    private String empId;
    private String currentLocation;
    private String userDept;
    private String externalId;
    private Integer authorityDaysRequired;



    public AssignedGrievanceDTO(Grievance grievance , String level, boolean canRespond) {
        this.id = grievance.getId();
        this.subject = grievance.getSubject();
        this.details = grievance.getDetails();
        this.status = String.valueOf(grievance.getStatus());
        this.submittedOn = grievance.getSubmittedOn();
        this.level = level;
        this.canRespond = canRespond;
        this.category = grievance.getCategory() != null ? grievance.getCategory().getName() : "";
        this.subcategory = grievance.getSubcategory() != null ? grievance.getSubcategory().getName() : "";
        this.locationName = grievance.getLocation() != null ? grievance.getLocation().getName() : "";
        this.userId = grievance.getUserId();
        this.concernedPersonName = grievance.getConcernedPersonName();
        this.userMobile = grievance.getUserMobile() != null ? grievance.getUserMobile() : "";
        this.anonymous = grievance.getAnonymous();
        this.empId = grievance.getEmpId() != null ? grievance.getEmpId() : "";
        this.currentLocation = grievance.getCurrentLocation() != null ? grievance.getCurrentLocation() :"";
        this.userDept = grievance.getUserDept() != null ? grievance.getUserDept() :"";
        this.externalId = grievance.getExternalId() != null ? grievance.getExternalId() :"";
        this.authorityDaysRequired = grievance.getAuthorityDaysRequired();

    }
}
