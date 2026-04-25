package com.xuka.exam.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and validation
 */
public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a random salt
     *
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[32];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash a password with salt using SHA-256
     *
     * @param password The plain text password
     * @param salt The salt (Base64 encoded)
     * @return Base64 encoded hash
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            
            // Combine password and salt
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            md.update(saltBytes);
            
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not found: " + ALGORITHM, e);
        }
    }

    /**
     * Verify a password against a stored hash
     *
     * @param password The plain text password to verify
     * @param salt The stored salt (Base64 encoded)
     * @param storedHash The stored hash (Base64 encoded)
     * @return true if password is correct, false otherwise
     */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String hashOfInput = hashPassword(password, salt);
        return hashOfInput.equals(storedHash);
    }
}
