package com.quickthought;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class NoteEncryption {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    
    /**
     * Creates an encryption key from a password using SHA-256
     */
    public static SecretKeySpec getKeyFromPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    /**
     * Encrypts plain text using AES encryption
     */
    public static String encrypt(String plainText, String password) throws Exception {
        SecretKeySpec key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    /**
     * Decrypts encrypted text using AES decryption
     */
    public static String decrypt(String encryptedText, String password) throws Exception {
        SecretKeySpec key = getKeyFromPassword(password);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, "UTF-8");
    }
    
    /**
     * Tests if a password can decrypt a test string
     */
    public static boolean testPassword(String testData, String password) {
        try {
            decrypt(testData, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
