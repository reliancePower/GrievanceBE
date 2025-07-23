package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CategoryMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String name;
}

