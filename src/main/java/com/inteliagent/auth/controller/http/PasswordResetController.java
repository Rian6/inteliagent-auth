package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user/password")
public class PasswordResetController {
    private final PasswordResetService service;
    public PasswordResetController(PasswordResetService service) { this.service = service; }
    public record Request(String email) {}
    public record Reset(String email, String code, String password) {}
    @PostMapping("/request")
    public ResponseEntity<?> request(@RequestBody Request request) {
        service.request(request.email());
        return ResponseEntity.accepted().body(Map.of("message", "Se a conta existir, você receberá instruções por email."));
    }
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody Reset request) {
        return service.reset(request.email(), request.code(), request.password())
            ? ResponseEntity.noContent().build()
            : ResponseEntity.badRequest().body(Map.of("message", "Código inválido ou expirado."));
    }
}
