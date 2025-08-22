package com.reliance.grievance.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportsAuthService {

    private final Set<String> superUsers;

    public ReportsAuthService(@Value("${app.reports.super-users:}") String csv) {
        this.superUsers = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public boolean isSuper(String userEmailOrId) {
        if (userEmailOrId == null) return false;
        return superUsers.contains(userEmailOrId.toLowerCase());
    }
}
