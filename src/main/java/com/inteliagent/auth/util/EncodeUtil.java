package com.inteliagent.auth.util;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public final class EncodeUtil {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);
    private EncodeUtil() {}
    public static byte[] hash(String password) {
        if (password == null || password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("A senha deve ter entre 12 caracteres e 72 bytes");
        return ENCODER.encode(password).getBytes(StandardCharsets.UTF_8);
    }
    public static boolean matches(String password, byte[] stored) {
        if (password == null || stored == null || password.getBytes(StandardCharsets.UTF_8).length > 72) return false;
        if (legacy(stored)) {
            try { return MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8)), stored); }
            catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        }
        return ENCODER.matches(password, new String(stored, StandardCharsets.UTF_8));
    }
    public static boolean legacy(byte[] stored) { return stored != null && stored.length == 32; }
    public static byte[] upgrade(String password) { return ENCODER.encode(password).getBytes(StandardCharsets.UTF_8); }
}
