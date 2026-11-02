package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.service.CedeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cede")
public class CedeController {
    public CedeController(CedeService cedeService) {
        this.cedeService = cedeService;
    }


    private final CedeService cedeService;

    @GetMapping("/validate")
    public ResponseEntity<?> validateCede(@RequestParam String cede) {
        try {
            List<Usuario> usuariosCedeAtual = cedeService.validateCede(Integer.parseInt(cede));
            return ResponseEntity.ok(usuariosCedeAtual);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
