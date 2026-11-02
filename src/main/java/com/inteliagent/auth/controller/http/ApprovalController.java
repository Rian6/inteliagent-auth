package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.configuration.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final NamedParameterJdbcTemplate db;
    public ApprovalController(NamedParameterJdbcTemplate db) { this.db = db; }
    private void requireAdmin() {
        if (!CurrentUser.admin()) throw new AccessDeniedException("Administrador necessário");
    }
    @GetMapping
    public List<Map<String, Object>> pending() {
        requireAdmin();
        return db.queryForList("SELECT id, nome, email FROM usuario WHERE id_cede=:cede AND situacao_aprovacao=0 ORDER BY created_at LIMIT 100", Map.of("cede", CurrentUser.cede()));
    }
    public record Decision(boolean approved) {}
    @PostMapping("/{id}")
    public void decide(@PathVariable int id, @RequestBody Decision decision) {
        requireAdmin();
        int count = db.update("UPDATE usuario SET situacao_aprovacao=:decision WHERE id=:id AND id_cede=:cede AND situacao_aprovacao=0",
                Map.of("id", id, "cede", CurrentUser.cede(), "decision", decision.approved() ? 1 : 2));
        if (count != 1) throw new IllegalArgumentException("Solicitação não encontrada");
    }
}
