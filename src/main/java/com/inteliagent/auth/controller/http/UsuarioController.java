package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.service.UsuarioService;
import com.inteliagent.auth.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/user")
public class UsuarioController {
    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil, com.inteliagent.auth.service.LoginSessionService sessions) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
        this.sessions = sessions;
    }


    private final UsuarioService usuarioService;

    private final JwtUtil jwtUtil;
    private final com.inteliagent.auth.service.LoginSessionService sessions;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Usuario usuario) {
        try {
            usuarioService.criarUsuario(usuario);
            return ResponseEntity.created(new URI("")).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Dados inválidos");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        try {
            Usuario userLogado = usuarioService.logar(usuario);

            if (userLogado != null) {
                String token = jwtUtil.generateToken(userLogado);
                userLogado.setToken(token);
                userLogado.setRefreshToken(sessions.issue(userLogado));

                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + token)
                        .body(userLogado);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        } catch (Exception e) {
            throw new IllegalArgumentException("Dados inválidos");
        }
    }


    @PostMapping("/validateRegisterUser")
    public ResponseEntity<?> validateRegisterUser(@RequestBody Usuario usuario) {
        try {
            usuarioService.validarUsuario(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Dados inválidos");
        }
    }

    @PostMapping("/validateCodeUserRegister")
    public ResponseEntity<?> validateCodeUserRegister(@RequestBody Usuario usuario) {
        try {
            Usuario newUser = usuarioService.validateCodeUser(usuario);
            return newUser == null
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Dados inválidos");
        }
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<?> senEmail(@RequestBody Usuario usuario) {
        try {
            usuarioService.sendEmail(usuario.getEmail());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            throw new IllegalArgumentException("Dados inválidos");
        }
    }
}
