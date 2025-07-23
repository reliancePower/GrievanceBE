package com.reliance.grievance.controller;


import com.reliance.grievance.dto.ErrorResponse;
import com.reliance.grievance.dto.LoginRequest;
import com.reliance.grievance.dto.LoginResponse;
import com.reliance.grievance.entity.EmployeeLogin;
import com.reliance.grievance.repository.EmployeeLoginRepository;
import com.reliance.grievance.service.AuthService;
import com.reliance.grievance.service.LoginAuditService;
import com.reliance.grievance.service.UserService;
import com.reliance.grievance.util.AESEncryptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/authenticate")

public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    AuthService authService;

    @Autowired
    private EmployeeLoginRepository employeeRepo;

    @Autowired
    LoginAuditService loginAuditService;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginController.class);


    @PostMapping("/webmail")
    public ResponseEntity<?> hrLogin(@RequestBody LoginRequest loginRequest , HttpServletRequest request) throws Exception {
        log.info("LoginController :: hrLogin() ");
        String password="";
        if(loginRequest.getType().equalsIgnoreCase("webmail")){
        String webmailID = loginRequest.getEmail();
        String password1 = loginRequest.getPassword();
        boolean exists = false;

        String decryptedPassword = authService.decryptPassword(password1);

        String ldapStatus = userService.checkLDAPAuth(webmailID, decryptedPassword);

        if ("T".equals(ldapStatus)) {
                loginAuditService.recordLogin(loginRequest.getEmail(),"webmail", request);
                return ResponseEntity.ok(new LoginResponse("Success", "Logged in successfully"));
        } else if ("F".equals(ldapStatus)) {
            return ResponseEntity.ok(new LoginResponse("Failure", "Invalid credentials"));
        }
        }
        else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Authentication failed", "Invalid credentials"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Authentication failed", "Invalid credentials"));
    }

    @PostMapping("/employee")
    public ResponseEntity<?> employeeLogin(@RequestBody Map<String, String> payload,HttpServletRequest request) {
        String empId = payload.get("employeeId");
        String encryptedPassword = payload.get("password");

        Optional<EmployeeLogin> employeeOpt = employeeRepo.findByEmployeeId(empId);

        if (employeeOpt.isPresent()) {
            EmployeeLogin employee = employeeOpt.get();
            try {
                String storedEncryptedPassword = employee.getPassword();

                String inputDecrypted = AESEncryptionUtil.decrypt(encryptedPassword);
                String dbDecrypted = AESEncryptionUtil.decrypt(storedEncryptedPassword);

                if (inputDecrypted.equals(dbDecrypted)) {
                    loginAuditService.recordLogin(empId,"employeeId", request);
                    return ResponseEntity.ok(Map.of("status", "Success", "employeeId", empId));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "Failure"));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "Decryption error"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "Employee not found"));
        }
    }

    @PostMapping("/logout-audit")
    public ResponseEntity<?> logoutAudit(@RequestBody Map<String, String> payload) {
        loginAuditService.recordLogout(payload.get("userId"), payload.get("loginType"));
        return ResponseEntity.ok("Logout recorded");
    }

}


