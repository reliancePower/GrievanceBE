package com.reliance.grievance.repository;

public interface ReportsRow {
    Long getId();
    String getSubject();
    String getStatus();
    java.time.LocalDateTime getSubmittedOn();
    String getLocationName();
    String getCategoryName();
    String getSubcategoryName();
    String getAssignedTo();      // maps concerned_person_name
    String getDetails();
    Integer getTatDays();
    java.time.LocalDateTime getUpdateDate();
}

