package com.reliance.grievance.service.impl;

import com.reliance.grievance.entity.Grievance;
import com.reliance.grievance.enums.GrievanceStatus;
import com.reliance.grievance.repository.GrievanceRepository;
import com.reliance.grievance.service.GrievanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GrievanceServiceImpl implements GrievanceService {

    @Autowired
    private GrievanceRepository grievanceRepo;

    @Override
    public void markAsResolved(Integer grievanceId) {
        Grievance grievance = grievanceRepo.findById(Long.valueOf(grievanceId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grievance not found"));

        grievance.setStatus(GrievanceStatus.RESOLVED);
        grievanceRepo.save(grievance);
    }
}

