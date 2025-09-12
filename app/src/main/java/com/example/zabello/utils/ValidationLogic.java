package com.example.zabello.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ValidationLogic {

    public static boolean isValidLogin(String s) {
        return s != null && s.trim().length() >= 3;
    }

    public static boolean isValidPassword(String s) {
        return s != null && s.length() >= 6;
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Нереально на Android, но fallback:
            throw new RuntimeException(e);
        }
    }
}
