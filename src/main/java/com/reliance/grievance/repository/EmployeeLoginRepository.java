package com.reliance.grievance.repository;

import com.reliance.grievance.entity.EmployeeLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeLoginRepository extends JpaRepository<EmployeeLogin, String> {
    Optional<EmployeeLogin> findByEmployeeIdAndPassword(String employeeId, String password);
    Optional<EmployeeLogin> findByEmployeeId(String employeeId);

}

