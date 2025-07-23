package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LevelMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Integer id;

    @Column(name = "level_name", nullable = false, length = 50)
    private String name;
}

