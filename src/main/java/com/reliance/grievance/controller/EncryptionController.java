package com.reliance.grievance.controller;

import com.reliance.grievance.util.AESEncryptionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/generate")
public class EncryptionController {

    @PostMapping("/encrypt-password")
    public ResponseEntity<?> encryptPassword(@RequestBody Map<String, String> payload) {
        try {
            String plainPassword = payload.get("password");
            if (plainPassword == null || plainPassword.isBlank()) {
                return ResponseEntity.badRequest().body("Password must be provided");
            }

            String encrypted = AESEncryptionUtil.encrypt(plainPassword);
            return ResponseEntity.ok(Map.of("encryptedPassword", encrypted));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encryption failed");
        }
    }
}

