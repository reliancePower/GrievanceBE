package com.reliance.grievance.util;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AesUtil {

    private final String key = "1234567890123456";

    public String decryptPassword(String encryptedPassword) throws Exception {
        String[] parts = encryptedPassword.split("::");
        String iv = parts[0];
        String ciphertext = parts[1];

        byte[] ivBytes = hexToByteArray(iv);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertextBytes = Base64.decodeBase64(ciphertext);

        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    private SecretKey generateKey(byte[] salt, String passphrase) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(passphrase.getBytes()); // Using passphrase as key
        return new SecretKeySpec(key, "AES");
    }

    private byte[] hexToByteArray(String hex) {
        try {
            return Hex.decodeHex(hex.toCharArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode hex string", e);
        }
    }
}


