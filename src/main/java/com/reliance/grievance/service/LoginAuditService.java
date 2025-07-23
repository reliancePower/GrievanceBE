package com.reliance.grievance.service;

import jakarta.servlet.http.HttpServletRequest;

public interface LoginAuditService {
    void recordLogin(String email, String webmail, HttpServletRequest request);

    void recordLogout(String userId, String loginType);
}
