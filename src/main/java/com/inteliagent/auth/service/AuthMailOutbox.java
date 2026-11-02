package com.inteliagent.auth.service;

import com.inteliagent.auth.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Durable mail delivery. Claims are atomic and SMTP runs outside request transactions. */
@Component
public class AuthMailOutbox {
    private final NamedParameterJdbcTemplate db;
    private final UsuarioRepository sender;
    public AuthMailOutbox(NamedParameterJdbcTemplate db, UsuarioRepository sender) {
        this.db = db;
        this.sender = sender;
    }
    public void enqueue(String email, String purpose, String code) {
        if (!purpose.equals("REGISTER") && !purpose.equals("RESET")) throw new IllegalArgumentException("Invalid mail purpose");
        db.update("INSERT INTO email_outbox(email,purpose,code,expires_at) VALUES(:email,:purpose,:code,DATE_ADD(NOW(6),INTERVAL 10 MINUTE))",
                Map.of("email",email,"purpose",purpose,"code",code));
    }
    @Scheduled(fixedDelayString="${mail.queue.poll-ms:500}")
    public void deliverDue() {
        var due = db.queryForList("SELECT id,email,purpose,code,attempts FROM email_outbox WHERE expires_at>NOW(6) AND attempts<5 AND ((state='PENDING' AND next_attempt_at<=NOW(6)) OR (state='SENDING' AND lease_until<NOW(6))) ORDER BY id LIMIT 10", Map.of());
        for (var item : due) {
            long id = ((Number)item.get("id")).longValue();
            if (db.update("UPDATE email_outbox SET state='SENDING',lease_until=DATE_ADD(NOW(6),INTERVAL 30 SECOND) WHERE id=:id AND expires_at>NOW(6) AND attempts<5 AND ((state='PENDING' AND next_attempt_at<=NOW(6)) OR (state='SENDING' AND lease_until<NOW(6)))", Map.of("id",id)) != 1) continue;
            String email=(String)item.get("email"), code=(String)item.get("code"), purpose=(String)item.get("purpose");
            try {
                if (stillValid(email,code,purpose)) sender.sendEmail(email,
                        purpose.equals("REGISTER") ? "Código de Verificação" : "Recuperação de senha Inteliagent",
                        purpose.equals("REGISTER") ? "Seu código Inteliagent: " + code + ". Válido por 10 minutos."
                                : "Seu código: " + code + ". Válido por 10 minutos. Se você não solicitou, ignore este email.");
                db.update("DELETE FROM email_outbox WHERE id=:id AND state='SENDING'",Map.of("id",id));
            } catch (Exception error) {
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Mail delivery deferred");
                int attempts=((Number)item.get("attempts")).intValue()+1;
                db.update("UPDATE email_outbox SET state='PENDING',attempts=:attempts,next_attempt_at=DATE_ADD(NOW(6),INTERVAL :delay SECOND),lease_until=NULL WHERE id=:id AND state='SENDING'",
                        Map.of("id",id,"attempts",attempts,"delay",Math.min(60,1 << attempts)));
            }
        }
        db.update("DELETE FROM email_outbox WHERE expires_at<=NOW(6) OR attempts>=5",Map.of());
    }
    private boolean stillValid(String email,String code,String purpose) throws Exception {
        if (purpose.equals("REGISTER")) return db.queryForObject("SELECT COUNT(*) FROM email_liberacao WHERE email=:email AND codigo=:code AND created_at>NOW()-INTERVAL 10 MINUTE",Map.of("email",email,"code",code),Integer.class)==1;
        if (!purpose.equals("RESET")) return false;
        byte[] digest=MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8));
        return db.queryForObject("SELECT COUNT(*) FROM password_reset WHERE email=:email AND code_hash=:hash AND expires_at>NOW()",Map.of("email",email,"hash",digest),Integer.class)==1;
    }
}
