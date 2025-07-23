package com.reliance.grievance.dto;

import lombok.Data;

@Data
public class SubCategoryDTO {
    private Integer id;
    private String name;

    public SubCategoryDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}

