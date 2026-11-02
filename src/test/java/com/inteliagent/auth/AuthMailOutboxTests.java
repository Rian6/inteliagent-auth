package com.inteliagent.auth;

import com.inteliagent.auth.repository.UsuarioRepository;
import com.inteliagent.auth.service.AuthMailOutbox;
import com.inteliagent.auth.service.PasswordResetService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthMailOutboxTests {
    private final NamedParameterJdbcTemplate db=mock(NamedParameterJdbcTemplate.class);
    private final UsuarioRepository sender=mock(UsuarioRepository.class);
    private final AuthMailOutbox queue=new AuthMailOutbox(db,sender);

    @Test void successfulDeliveryRemovesItsPendingCode() {
        when(db.queryForList(startsWith("SELECT id,email,purpose,code"),anyMap())).thenReturn(List.of(
                Map.of("id",17L,"email","someone@example.test","purpose","REGISTER","code","123456","attempts",0)));
        when(db.update(startsWith("UPDATE email_outbox SET state='SENDING'"),anyMap())).thenReturn(1);
        when(db.queryForObject(startsWith("SELECT COUNT(*) FROM email_liberacao"),anyMap(),eq(Integer.class))).thenReturn(1);
        queue.deliverDue();
        verify(sender).sendEmail(eq("someone@example.test"),anyString(),contains("123456"));
        verify(db).update(startsWith("DELETE FROM email_outbox WHERE id="),eq(Map.of("id",17L)));
    }

    @Test void obsoleteCodesAreDroppedWithoutEmail() {
        when(db.queryForList(startsWith("SELECT id,email,purpose,code"),anyMap())).thenReturn(List.of(
                Map.of("id",18L,"email","someone@example.test","purpose","REGISTER","code","123456","attempts",0)));
        when(db.update(startsWith("UPDATE email_outbox SET state='SENDING'"),anyMap())).thenReturn(1);
        when(db.queryForObject(startsWith("SELECT COUNT(*) FROM email_liberacao"),anyMap(),eq(Integer.class))).thenReturn(0);
        queue.deliverDue();
        verifyNoInteractions(sender);
        verify(db).update(startsWith("DELETE FROM email_outbox WHERE id="),eq(Map.of("id",18L)));
    }

    @Test void smtpFailureKeepsWorkForRetry() {
        when(db.queryForList(startsWith("SELECT id,email,purpose,code"),anyMap())).thenReturn(List.of(
                Map.of("id",19L,"email","someone@example.test","purpose","REGISTER","code","123456","attempts",0)));
        when(db.update(startsWith("UPDATE email_outbox SET state='SENDING'"),anyMap())).thenReturn(1);
        when(db.queryForObject(startsWith("SELECT COUNT(*) FROM email_liberacao"),anyMap(),eq(Integer.class))).thenReturn(1);
        doThrow(new IllegalStateException("SMTP offline")).when(sender).sendEmail(anyString(),anyString(),anyString());
        queue.deliverDue();
        verify(db).update(startsWith("UPDATE email_outbox SET state='PENDING'"),argThat((Map<String,?> args) ->
                args.get("id").equals(19L) && args.get("attempts").equals(1)));
    }

    @Test void resetRequestQueuesOnlyExistingAccountsAndNeverCallsSmtp() {
        var reset=new PasswordResetService(db,queue);
        when(db.queryForObject(startsWith("SELECT COUNT(*) FROM usuario"),anyMap(),eq(Integer.class))).thenReturn(1);
        when(db.queryForObject(startsWith("SELECT COUNT(*) FROM password_reset"),anyMap(),eq(Integer.class))).thenReturn(0);
        reset.request("someone@example.test");
        verify(db).update(startsWith("INSERT INTO password_reset"),anyMap());
        verify(db).update(startsWith("INSERT INTO email_outbox"),argThat((Map<String,?> args) ->
                args.get("purpose").equals("RESET") && ((String)args.get("code")).matches("[0-9]{6}")));
        verifyNoInteractions(sender);
    }
}
