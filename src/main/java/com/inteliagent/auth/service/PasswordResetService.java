package com.inteliagent.auth.service;

import com.inteliagent.auth.repository.UsuarioRepository;
import com.inteliagent.auth.util.EncodeUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
    private final NamedParameterJdbcTemplate db;
    private final AuthMailOutbox mail;
    private final SecureRandom random = new SecureRandom();
    public PasswordResetService(NamedParameterJdbcTemplate db, AuthMailOutbox mail) { this.db = db; this.mail = mail; }
    private byte[] digest(String code) {
        try { return MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8)); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    @Transactional
    public void request(String email) {
        if (email == null || email.length() > 254) return;
        var params = Map.of("email", email);
        if (db.queryForObject("SELECT COUNT(*) FROM usuario WHERE email=:email AND situacao=1", params, Integer.class) != 1) return;
        if (db.queryForObject("SELECT COUNT(*) FROM password_reset WHERE email=:email AND requested_at > NOW() - INTERVAL 1 MINUTE", params, Integer.class) > 0) return;
        String code = String.valueOf(100000 + random.nextInt(900000));
        db.update("INSERT INTO password_reset (email,code_hash,expires_at) VALUES (:email,:hash,NOW() + INTERVAL 10 MINUTE) ON DUPLICATE KEY UPDATE code_hash=:hash, expires_at=NOW() + INTERVAL 10 MINUTE, attempts=0, requested_at=NOW()", Map.of("email",email,"hash",digest(code)));
        mail.enqueue(email,"RESET",code);
    }
    @Transactional
    public boolean reset(String email, String code, String password) {
        if (email == null || code == null || !code.matches("[0-9]{6}")) return false;
        byte[] hash = EncodeUtil.hash(password);
        var rows = db.queryForList("SELECT code_hash, attempts FROM password_reset WHERE email=:email AND expires_at > NOW() FOR UPDATE", Map.of("email",email));
        if (rows.isEmpty() || ((Number) rows.get(0).get("attempts")).intValue() >= 5) return false;
        // Return normally after a failed attempt so the counter is committed.
        if (!MessageDigest.isEqual((byte[]) rows.get(0).get("code_hash"), digest(code))) {
            db.update("UPDATE password_reset SET attempts=attempts+1 WHERE email=:email", Map.of("email",email));
            return false;
        }
        db.update("UPDATE usuario SET senha=:password, token_version=token_version+1 WHERE email=:email", Map.of("email",email,"password",hash));
        db.update("DELETE FROM password_reset WHERE email=:email", Map.of("email",email));
        return true;
    }
}
