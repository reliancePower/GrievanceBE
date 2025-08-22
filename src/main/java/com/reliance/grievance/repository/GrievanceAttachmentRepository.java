package com.reliance.grievance.repository;

import com.reliance.grievance.entity.GrievanceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrievanceAttachmentRepository extends JpaRepository<GrievanceAttachment, Long> {
    List<GrievanceAttachment> findByGrievanceId(Integer grievanceId);
    List<GrievanceAttachment> findByGrievanceIdOrderByUploadedOnDesc(Long grievanceId);
}

