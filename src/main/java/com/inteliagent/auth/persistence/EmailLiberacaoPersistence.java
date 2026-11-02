package com.inteliagent.auth.persistence;
import com.inteliagent.auth.entity.EmailLiberacao;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.namedparam.*;
@Repository
public class EmailLiberacaoPersistence {
    private final NamedParameterJdbcTemplate db;
    public EmailLiberacaoPersistence(NamedParameterJdbcTemplate db) { this.db = db; }
    public void save(EmailLiberacao code) {
        var params = new MapSqlParameterSource("email", code.getEmail()).addValue("codigo", code.getCodigo());
        db.update("DELETE FROM email_liberacao WHERE email = :email", params);
        db.update("INSERT INTO email_liberacao (email, codigo) VALUES (:email, :codigo)", params);
    }
    @org.springframework.transaction.annotation.Transactional
    public boolean findByEmail(String email, String code) {
        if (code == null || !code.matches("[0-9]{6}")) return false;
        var params = new MapSqlParameterSource("email", email);
        var rows = db.queryForList("SELECT codigo, attempts FROM email_liberacao WHERE email=:email AND created_at > NOW() - INTERVAL 10 MINUTE FOR UPDATE", params);
        if (rows.isEmpty() || ((Number) rows.getFirst().get("attempts")).intValue() >= 5) return false;
        if (String.valueOf(rows.getFirst().get("codigo")).equals(code)) return true;
        db.update("UPDATE email_liberacao SET attempts=attempts+1 WHERE email=:email", params);
        return false;
    }
    public boolean consume(String email, String code) {
        if (code == null || !code.matches("[0-9]{6}")) return false;
        return db.update("DELETE FROM email_liberacao WHERE email = :email AND codigo = :codigo AND attempts < 5 AND created_at > NOW() - INTERVAL 10 MINUTE",
            new MapSqlParameterSource("email", email).addValue("codigo", code)) == 1;
    }
}
