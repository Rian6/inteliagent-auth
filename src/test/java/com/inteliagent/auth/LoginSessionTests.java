package com.inteliagent.auth;
import com.inteliagent.auth.controller.http.LoginSessionController;
import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.service.LoginSessionService;
import com.inteliagent.auth.service.UsuarioService;
import com.inteliagent.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class LoginSessionTests {
    @Test void browserLoginKeepsRenewalCredentialInHttpOnlyCookie() throws Exception {
        var users=mock(UsuarioService.class);var sessions=mock(LoginSessionService.class);var jwt=mock(JwtUtil.class);
        var input=new Usuario();var user=new Usuario();user.setId(1);user.setIdCede(2);
        when(users.logar(input)).thenReturn(user);when(sessions.issue(user)).thenReturn("R".repeat(43));when(jwt.generateToken(user)).thenReturn("access");
        var response=new LoginSessionController(sessions,users,jwt,false).loginWeb(input);
        String cookie=response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertTrue(cookie.contains("HttpOnly"));assertTrue(cookie.contains("SameSite=Strict"));assertTrue(cookie.contains("Max-Age=2592000"));
        assertNull(((Usuario)response.getBody()).getRefreshToken());
        assertEquals("access",((Usuario)response.getBody()).getToken());
    }
    @Test void invalidBrowserLoginDoesNotIssuePersistentSession() throws Exception {
        var users=mock(UsuarioService.class);var sessions=mock(LoginSessionService.class);var jwt=mock(JwtUtil.class);
        var response=new LoginSessionController(sessions,users,jwt,false).loginWeb(new Usuario());
        assertEquals(401,response.getStatusCode().value());verifyNoInteractions(sessions);
    }
}
