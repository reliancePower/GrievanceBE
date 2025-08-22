package com.reliance.grievance.repository;

import com.reliance.grievance.entity.ConcernedAuthorityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ConcernedAuthorityRepository extends JpaRepository<ConcernedAuthorityMaster,Long> {
    Optional<ConcernedAuthorityMaster> findByLocationIdAndCategoryIdAndSubcategoryIdAndLevel(
            Integer locationId, Integer categoryId, Integer subcategoryId, String level
    );
    boolean existsByEmailIgnoreCase(String email);

    List<ConcernedAuthorityMaster> findByLocationIdAndCategoryIdAndSubcategoryIdOrderByLevelAsc(
            Integer locationId,
            Integer categoryId,
            Integer subcategoryId
    );

    List<ConcernedAuthorityMaster> findByEmployeeId(String employeeId);
    List<ConcernedAuthorityMaster> findByNameIgnoreCase(String email);

    Optional<ConcernedAuthorityMaster> findFirstByEmailIgnoreCase(String email);


}
