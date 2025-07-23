package com.reliance.grievance.repository;

import com.reliance.grievance.entity.ConcernedAuthorityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ConcernedAuthorityRepository extends JpaRepository<ConcernedAuthorityMaster,Long> {
    Optional<ConcernedAuthorityMaster> findByLocationIdAndCategoryIdAndSubcategoryIdAndLevel(
            Integer locationId, Integer categoryId, Integer subcategoryId, String level
    );
    boolean existsByEmailIgnoreCase(String email);




}
