package com.reliance.grievance.repository;

import com.reliance.grievance.entity.SubCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryMasterRepository extends JpaRepository<SubCategoryMaster, Long> {
    List<SubCategoryMaster> findByCategoryId(Long categoryId);
}
