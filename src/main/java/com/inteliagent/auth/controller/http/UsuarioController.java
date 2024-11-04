package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.service.UsuarioService;
import com.inteliagent.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/user")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Usuario usuario) {
        try {
            usuarioService.criarUsuario(usuario);
            return ResponseEntity.created(new URI("")).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao registrar usuário: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        try {
            Usuario userLogado = usuarioService.logar(usuario);

            if (userLogado != null) {
                String token = jwtUtil.generateToken(userLogado);

                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + token)
                        .body(userLogado);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao logar usuário: " + e.getMessage());
        }
    }


    @PostMapping("/validateRegisterUser")
    public ResponseEntity<?> validateRegisterUser(@RequestBody Usuario usuario) {
        try {
            usuarioService.validarUsuario(usuario);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao registrar usuário: " + e.getMessage());
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
            return ResponseEntity.badRequest().body("Erro ao registrar usuário: " + e.getMessage());
        }
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<?> senEmail(@RequestBody Usuario usuario) {
        try {
            usuarioService.sendEmail(usuario.getEmail());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao enviar email: " + e.getMessage());
        }
    }
}
