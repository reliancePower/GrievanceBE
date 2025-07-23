package com.reliance.grievance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceRequestDTO {
    private Integer levelId;
    private Integer locationId;
    private String currentLocation;
    private String subject;
    private Integer categoryId;
    private Integer subcategoryId;
    private String details;
    private String anonymous;
    private String type;
    private String userId;
    private String email;
}

