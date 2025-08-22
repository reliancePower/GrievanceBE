package com.reliance.grievance.service;

import com.reliance.grievance.dto.HrDirectoryInfo;

import java.util.Optional;

public interface UserDirectoryService {
    Optional<String> findMobileByEmail(String email);
    Optional<String> findPrNoByEmail(String email);

    boolean isAllowedUser(String email);

    Optional<HrDirectoryInfo> findHrInfoByEmail(String email);
}

