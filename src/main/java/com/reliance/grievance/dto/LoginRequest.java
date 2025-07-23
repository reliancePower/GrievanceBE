package com.reliance.grievance.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String type;
    private String password;
    private String employeeId;
    private String email;
}
