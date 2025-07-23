package com.reliance.grievance.service.impl;

import com.reliance.grievance.service.AuthService;
import com.reliance.grievance.util.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    AesUtil aesUtil;
    public String decryptPassword(String encryptedPassword) throws Exception {
        return aesUtil.decryptPassword(encryptedPassword);
    }
}
