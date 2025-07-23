package com.reliance.grievance.controller;

import com.reliance.grievance.dto.SubCategoryDTO;
import com.reliance.grievance.entity.CategoryMaster;
import com.reliance.grievance.entity.LevelMaster;
import com.reliance.grievance.entity.LocationMaster;
import com.reliance.grievance.entity.SubCategoryMaster;
import com.reliance.grievance.repository.CategoryMasterRepository;
import com.reliance.grievance.repository.LevelMasterRepository;
import com.reliance.grievance.repository.LocationMasterRepository;
import com.reliance.grievance.repository.SubCategoryMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/master")
@CrossOrigin
public class MasterController {

    @Autowired
    private LevelMasterRepository levelRepo;
    @Autowired private LocationMasterRepository locationRepo;
    @Autowired private CategoryMasterRepository categoryRepo;
    @Autowired private SubCategoryMasterRepository subCategoryRepo;

    @GetMapping("/levels")
    public List<LevelMaster> getLevels() {
        return levelRepo.findAll();
    }

    @GetMapping("/locations")
    public List<LocationMaster> getLocations() {
        return locationRepo.findAll();
    }

    @GetMapping("/categories")
    public List<CategoryMaster> getCategories() {
        return categoryRepo.findAll();
    }

    @GetMapping("/subcategories/{categoryId}")
    public List<SubCategoryDTO> getSubcategories(@PathVariable Integer categoryId) {
        return subCategoryRepo.findByCategoryId(Long.valueOf(categoryId))
                .stream()
                .map(sc -> new SubCategoryDTO(sc.getId(), sc.getName()))
                .collect(Collectors.toList());
    }

}

