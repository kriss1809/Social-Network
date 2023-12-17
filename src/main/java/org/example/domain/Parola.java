package org.example.domain;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Objects;
import java.security.SecureRandom;

public class Parola extends Entity<Long>{

    String parola;
    String username;

    public Parola(String parola, String username) {
        this.parola = parola;
        this.username = username;
    }

    public String getParola() {
        return parola;
    }

    public void setParola(String parola) {
        this.parola = parola;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Parola parola1 = (Parola) o;
        return Objects.equals(parola, parola1.parola);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parola);
    }

    public String criptare() {
        try {
            // Generare cheie secretă
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(parola.toCharArray(), "Salt".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            // Criptare
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(parola.getBytes());

            // Convertire în reprezentare base64
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace(); // Tratarea erorilor
            return null;
        }
    }

    public String decriptare(String parolaCriptata) {
        try {
            // Generare cheie secretă
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(parola.toCharArray(), "Salt".getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            // Decriptare
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(parolaCriptata));

            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace(); // Tratarea erorilor
            return null;
        }
    }
}
