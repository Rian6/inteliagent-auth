package com.inteliagent.auth.configuration;

import com.inteliagent.auth.configuration.SecurityConfig;
import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.util.JwtUtil;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TokenSecurityTests {
    @Test void integerClaimsAreBoundToTheDatabaseWithoutStringCasts() {
        var key = new SecretKeySpec(new byte[32], "HmacSHA256");
        var db = mock(NamedParameterJdbcTemplate.class);
        when(db.queryForList(anyString(), org.mockito.ArgumentMatchers.<Map<String, ?>>any())).thenReturn(List.of(Map.of("id", 7)));
        var user = new Usuario(); user.setId(7); user.setIdCede(2); user.setEmail("test@local.invalid"); user.setRole("ADMIN");
        String token = new JwtUtil(key, "inteliagent-auth").generateToken(user);
        var decoder = new SecurityConfig().jwtDecoder(key, "inteliagent-auth", db);
        assertEquals("test@local.invalid", decoder.decode(token).getSubject());
        verify(db).queryForList(anyString(), org.mockito.ArgumentMatchers.<Map<String, ?>>argThat(values -> values.get("uid") instanceof Number && values.get("version") instanceof Number));
    }
}
