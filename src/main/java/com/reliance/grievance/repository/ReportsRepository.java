package com.reliance.grievance.repository;

import com.reliance.grievance.entity.Grievance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReportsRepository extends Repository<Grievance, Long> {

    @Query(value = """
            SELECT
              r.id                    AS id,
              r.subject               AS subject,
              r.status                AS status,
              r.submitted_on          AS submittedOn,
              r.location_name         AS locationName,
              r.category_name         AS categoryName,
              r.subcategory_name      AS subcategoryName,
              r.concerned_person_name AS assignedTo,
              r.details               AS details,
              r.update_date           AS updateDate,
              r.tat_days AS tatDays
            FROM dbo.GrievanceStatus r
            WHERE r.submitted_on >= :from AND r.submitted_on < :to
            ORDER BY r.submitted_on DESC
            """,
            countQuery = """
                    SELECT COUNT(1)
                    FROM dbo.GrievanceStatus r
                    WHERE r.submitted_on >= :from AND r.submitted_on < :to
                    """,
            nativeQuery = true)
    Page<ReportsRow> findBetween(@Param("from") java.time.LocalDateTime from,
                                 @Param("to") java.time.LocalDateTime to,
                                 org.springframework.data.domain.Pageable pageable);


}

