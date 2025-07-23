package com.reliance.grievance.repository;

import com.reliance.grievance.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    Optional<LoginAudit> findTopByUserIdAndLoginTypeAndActiveOrderByLoginTimeDesc(
            String userId, String loginType, String active);
}

