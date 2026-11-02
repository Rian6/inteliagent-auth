package com.inteliagent.auth.service;

import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.persistence.UsuarioPersistence;
import com.inteliagent.auth.util.JwtUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginSessionService {
    private final NamedParameterJdbcTemplate db;
    private final UsuarioPersistence users;
    private final JwtUtil jwt;
    private final SecureRandom random = new SecureRandom();
    public LoginSessionService(NamedParameterJdbcTemplate db, UsuarioPersistence users, JwtUtil jwt) {
        this.db = db; this.users = users; this.jwt = jwt;
    }
    private byte[] hash(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_-]{43}")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
        catch (java.security.NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    @Transactional
    public String issue(Usuario user) {
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        db.update("DELETE FROM auth_sessions WHERE user_id=:id AND expires_at<NOW()", Map.of("id",user.getId()));
        db.update("INSERT INTO auth_sessions(token_hash,user_id,id_cede,token_version,expires_at) VALUES(:hash,:id,:cede,:version,NOW()+INTERVAL 30 DAY)",
            Map.of("hash",hash(token),"id",user.getId(),"cede",user.getIdCede(),"version",user.getTokenVersion()));
        return token;
    }
    @Transactional(readOnly=true)
    public Usuario renew(String token) {
        var sessions = db.queryForList("SELECT u.email FROM auth_sessions s JOIN usuario u ON u.id=s.user_id AND u.id_cede=s.id_cede AND u.token_version=s.token_version WHERE s.token_hash=:hash AND s.revoked_at IS NULL AND s.expires_at>NOW() AND u.situacao=1 AND u.situacao_aprovacao=1", Map.of("hash",hash(token)));
        if (sessions.size()!=1) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Usuario user = users.login((String)sessions.getFirst().get("email"));
        user.setSenha(null); user.setSecurityPassword(null); user.setCodigoEmail(null);
        user.setToken(jwt.generateToken(user));
        return user;
    }
    @Transactional
    public void revoke(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return;
        db.update("UPDATE auth_sessions SET revoked_at=NOW() WHERE token_hash=:hash AND revoked_at IS NULL",Map.of("hash",hash(token)));
    }
}
