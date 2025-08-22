package com.reliance.grievance.dto;

import com.reliance.grievance.entity.ConcernedAuthorityMaster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConcernedAuthorityDTO {
    private String level;
    private String name;
    private String email;
    private Integer days;


    public ConcernedAuthorityDTO(ConcernedAuthorityMaster entity) {
        this.level = entity.getLevel();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.days = entity.getDays();
    }
}

