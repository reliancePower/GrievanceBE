package com.reliance.grievance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "employee_login")
@Data
public class EmployeeLogin {

    @Id
    @Column(name = "employee_id")
    private String employeeId;

    private String name;

    private String password;
}

