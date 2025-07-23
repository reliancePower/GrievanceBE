package com.reliance.grievance.service.impl;

import com.reliance.grievance.entity.LoginAudit;
import com.reliance.grievance.repository.LoginAuditRepository;
import com.reliance.grievance.service.LoginAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginAuditServiceImpl implements LoginAuditService {

    @Autowired
    private LoginAuditRepository auditRepo;

    public void recordLogin(String userId, String loginType, HttpServletRequest request) {
        LoginAudit audit = new LoginAudit();
        audit.setUserId(userId);
        audit.setLoginType(loginType);
        audit.setLoginTime(LocalDateTime.now());
        audit.setActive("Y");
        audit.setIpAddress(request.getRemoteAddr());
        audit.setUserAgent(request.getHeader("User-Agent"));
        auditRepo.save(audit);
    }

    public void recordLogout(String userId, String loginType) {
        Optional<LoginAudit> lastSession = auditRepo
                .findTopByUserIdAndLoginTypeAndActiveOrderByLoginTimeDesc(userId, loginType, "Y");

        if (lastSession.isPresent()) {
            LoginAudit audit = lastSession.get();
            audit.setLogoutTime(LocalDateTime.now());
            audit.setActive("N");
            auditRepo.save(audit);
        }
    }
}
