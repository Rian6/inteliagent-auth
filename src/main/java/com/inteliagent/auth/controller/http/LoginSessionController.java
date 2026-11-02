package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.dto.auth.RefreshTokenRequest;
import com.inteliagent.auth.service.LoginSessionService;
import com.inteliagent.auth.service.UsuarioService;
import com.inteliagent.auth.util.JwtUtil;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class LoginSessionController {
    private final LoginSessionService sessions;
    private final UsuarioService users;
    private final JwtUtil jwt;
    private final boolean secure;
    public LoginSessionController(LoginSessionService sessions, UsuarioService users, JwtUtil jwt, @Value("${security.session.cookie-secure:false}") boolean secure) {
        this.sessions=sessions; this.users=users; this.jwt=jwt; this.secure=secure;
    }
    private String cookie(String value, Duration age) {
        return ResponseCookie.from("inteliagent_session",value).httpOnly(true).secure(secure).sameSite("Strict").path("/api/user").maxAge(age).build().toString();
    }
    @PostMapping("/login/web")
    public ResponseEntity<?> loginWeb(@RequestBody Usuario credentials) throws Exception {
        Usuario user=users.logar(credentials);
        if(user==null) return ResponseEntity.status(401).build();
        String refresh=sessions.issue(user);
        user.setToken(jwt.generateToken(user));
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,cookie(refresh,Duration.ofDays(30))).body(user);
    }
    @PostMapping("/refresh/web")
    public Usuario refreshWeb(@CookieValue(name="inteliagent_session",required=false) String token) { return sessions.renew(token); }
    @PostMapping("/logout/web")
    public ResponseEntity<Void> logoutWeb(@CookieValue(name="inteliagent_session",required=false) String token) {
        sessions.revoke(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE,cookie("",Duration.ZERO)).build();
    }
    @PostMapping("/refresh")
    public Usuario refresh(@RequestBody RefreshTokenRequest request) { return sessions.renew(request.getRefreshToken()); }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) { sessions.revoke(request.getRefreshToken()); return ResponseEntity.noContent().build(); }
}
