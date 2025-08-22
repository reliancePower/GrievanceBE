package com.reliance.grievance.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ConcernedAuthorityMaster",
        uniqueConstraints = @UniqueConstraint(columnNames = {"location_id", "category_id", "level"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcernedAuthorityMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "authority_id")
    private Integer id;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "level", nullable = false, length = 10)
    private String level;  // Expected values: L1, L2, L3

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationMaster location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryMaster category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategoryMaster subcategory;


    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @Column(name = "days")
    private Integer days;

    @Column(name = "is_super", length = 1, nullable = false)
    private String isSuper = "N";

}

