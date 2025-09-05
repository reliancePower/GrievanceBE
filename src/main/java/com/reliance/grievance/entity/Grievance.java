package com.reliance.grievance.entity;

import com.reliance.grievance.enums.GrievanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Grievance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_id")
    private Integer levelId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "current_location")
    private String currentLocation;

    @Column(name = "subject")
    private String subject;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "subcategory_id")
    private Integer subcategoryId;

    @Column(name = "details", length = 2000)
    private String details;

    @Column(name = "submitted_on")
    private LocalDateTime submittedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GrievanceStatus status;


    @Column(name = "anonymous", length = 1)
    private String anonymous;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "hostname")
    private String hostname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private CategoryMaster category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", insertable = false, updatable = false)
    private SubCategoryMaster subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concerned_authority_id")
    private ConcernedAuthorityMaster concernedAuthority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @Column(name = "concerned_person_email")
    private String concernedPersonEmail;

    @Column(name = "concerned_person_name")
    private String concernedPersonName;

    @Column(name = "concerned_person_empid")
    private String concernedPersonEmpId;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "user_rating_submitted")
    private Boolean userRatingSubmitted = false;

    @Column(name = "user_mobile", length = 20)
    private String userMobile;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Column(name = "emp_id")
    private String empId;

    @Column(name = "user_dept")
    private String userDept;

    @Column(name = "authority_days_required")
    private Integer authorityDaysRequired;



    @PreUpdate
    public void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    @PrePersist
    public void onCreate() {
        this.updateDate = LocalDateTime.now();
    }


}

