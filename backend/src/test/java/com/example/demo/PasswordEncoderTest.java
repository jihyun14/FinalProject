package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 
        String rawPassword = "password123"; 
        String hashedPassword = encoder.encode(rawPassword);

        // 
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Hashed Password: " + hashedPassword);
        // 
    }
}
