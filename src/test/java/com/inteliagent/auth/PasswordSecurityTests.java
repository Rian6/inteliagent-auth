package com.inteliagent.auth;

import com.inteliagent.auth.util.EncodeUtil;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import static org.junit.jupiter.api.Assertions.*;

class PasswordSecurityTests {
    @Test void passwordsUseSaltAndVerifyCorrectly() {
        var first = EncodeUtil.hash("a-strong-password-2026");
        var second = EncodeUtil.hash("a-strong-password-2026");
        assertFalse(java.util.Arrays.equals(first, second));
        assertTrue(EncodeUtil.matches("a-strong-password-2026", first));
        assertFalse(EncodeUtil.matches("incorrect-password", first));
        assertFalse(EncodeUtil.legacy(first));
    }
    @Test void legacyPasswordsCanBeUpgradedAfterVerification() throws Exception {
        byte[] old = MessageDigest.getInstance("SHA-256").digest("legacy-password".getBytes(StandardCharsets.UTF_8));
        assertTrue(EncodeUtil.legacy(old));
        assertTrue(EncodeUtil.matches("legacy-password", old));
        assertFalse(EncodeUtil.matches("wrong", old));
        assertTrue(EncodeUtil.matches("legacy-password", EncodeUtil.upgrade("legacy-password")));
    }
    @Test void passwordsCannotBeSilentlyTruncated() {
        assertThrows(IllegalArgumentException.class, () -> EncodeUtil.hash("short"));
        assertThrows(IllegalArgumentException.class, () -> EncodeUtil.hash("é".repeat(40)));
        assertFalse(EncodeUtil.matches(null, new byte[32]));
    }
}
