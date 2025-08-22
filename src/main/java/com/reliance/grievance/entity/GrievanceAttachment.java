package com.reliance.grievance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "grievance_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grievance_id")
    private Grievance grievance;

    @Lob
    private byte[] data;

    private String fileName;

    private String fileType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "uploaded_on")
    private LocalDateTime uploadedOn;
}

