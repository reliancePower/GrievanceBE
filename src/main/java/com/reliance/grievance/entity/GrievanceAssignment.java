package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grievance_id", nullable = false)
    private Grievance grievance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", nullable = false)
    private ConcernedAuthorityMaster authority;

    @Column(name = "level_label", length = 10, nullable = false)
    private String level;

    @Column(name = "assigned_on", nullable = false)
    private LocalDateTime assignedOn;

    @Column(name = "unassigned_on")
    private LocalDateTime unassignedOn;

    @Column(name = "active", nullable = false)
    private boolean active;
}

