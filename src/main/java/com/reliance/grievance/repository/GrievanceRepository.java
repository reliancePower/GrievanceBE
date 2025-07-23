package com.reliance.grievance.repository;

import com.reliance.grievance.entity.Grievance;
import com.reliance.grievance.enums.GrievanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long> {
    List<Grievance> findByUserIdIgnoreCase(String userId);

    @Query("SELECT g FROM Grievance g WHERE LOWER(TRIM(g.concernedPersonName)) = LOWER(TRIM(:name))")
    List<Grievance> findByConcernedPersonName(@Param("name") String name);


    // OR: Search by employee ID
    List<Grievance> findByConcernedPersonEmpId(String employeeId);

    List<Grievance> findAllByStatusNot(GrievanceStatus status);



}

