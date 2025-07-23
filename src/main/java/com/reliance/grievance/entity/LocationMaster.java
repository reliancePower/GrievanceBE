package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LocationMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Integer id;

    @Column(name = "location_name", nullable = false, length = 100)
    private String name;
}

