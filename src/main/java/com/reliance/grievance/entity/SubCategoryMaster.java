package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SubCategoryMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subcategory_id")
    private Integer id;

    @Column(name = "subcategory_name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryMaster category;
}

