package com.reliance.grievance.repository;

import com.reliance.grievance.entity.GrievanceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GrievanceAssignmentRepo extends JpaRepository<GrievanceAssignment, Long> {

    @Query("""
     select ga from GrievanceAssignment ga
     join fetch ga.grievance g
     join fetch ga.authority a
     where (lower(a.email) = lower(:email) or a.employeeId = :empId)
     order by ga.assignedOn desc
  """)
    List<GrievanceAssignment> findByAuthority(@Param("email") String email,
                                              @Param("empId") String empId);

    List<GrievanceAssignment> findByGrievanceIdOrderByAssignedOnAsc(Long grievanceId);

    Optional<GrievanceAssignment> findByGrievanceIdAndActiveTrue(Long grievanceId);
}

