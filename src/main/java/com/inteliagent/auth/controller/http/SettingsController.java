package com.inteliagent.auth.controller.http;
import com.inteliagent.auth.configuration.CurrentUser;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/user/settings")
public class SettingsController {
    private final NamedParameterJdbcTemplate db;
    public SettingsController(NamedParameterJdbcTemplate db) { this.db=db; }
    private void requireAdmin() { if(!CurrentUser.admin()) throw new AccessDeniedException("Administrador necessário"); }
    public record Settings(boolean autoApprove) {}
    @GetMapping public Settings get() {
        requireAdmin();
        return new Settings(Boolean.TRUE.equals(db.queryForObject("SELECT AUTO_APROVAR FROM cede WHERE ID=:id",Map.of("id",CurrentUser.cede()),Boolean.class)));
    }
    @PutMapping public void save(@RequestBody Settings settings) {
        requireAdmin();
        db.update("UPDATE cede SET AUTO_APROVAR=:value WHERE ID=:id",Map.of("value",settings.autoApprove(),"id",CurrentUser.cede()));
    }
}
