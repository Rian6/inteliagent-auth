package com.inteliagent.auth.controller.http;
import com.inteliagent.auth.configuration.CurrentUser;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
@RestController @RequestMapping("/api/user")
public class AgentPermissionsController {
 private final NamedParameterJdbcTemplate db;
 public AgentPermissionsController(NamedParameterJdbcTemplate db){this.db=db;}
 private void admin(){if(!CurrentUser.admin())throw new AccessDeniedException("Administrador necessário");}
 @GetMapping("/permissions") public Map<String,Object> mine(){return db.queryForMap("SELECT PODE_CRIAR_PLANEJAMENTO AS createPlanning,PODE_EDITAR_PLANEJAMENTO AS editPlanning,PODE_ADICIONAR_VISITAS AS addVisits,PODE_REORDENAR_VISITAS AS reorderVisits FROM USUARIO WHERE ID=:id AND ID_CEDE=:cede",Map.of("id",CurrentUser.id(),"cede",CurrentUser.cede()));}
 @GetMapping({"/planning-permissions","/agent-permissions"}) public List<Map<String,Object>> list(){admin();return db.queryForList("SELECT ID AS id,NOME AS nome,EMAIL AS email,ROLE AS role,PODE_CRIAR_PLANEJAMENTO AS createPlanning,PODE_EDITAR_PLANEJAMENTO AS editPlanning,PODE_ADICIONAR_VISITAS AS addVisits,PODE_REORDENAR_VISITAS AS reorderVisits FROM USUARIO WHERE ID_CEDE=:cede AND SITUACAO=1 AND SITUACAO_APROVACAO=1 ORDER BY NOME,ID",Map.of("cede",CurrentUser.cede()));}
 public record Permissions(boolean createPlanning,boolean editPlanning,Boolean addVisits,Boolean reorderVisits){}
 @PutMapping({"/planning-permissions/{id}","/agent-permissions/{id}"}) public void update(@PathVariable int id,@RequestBody Permissions value){admin();int n=db.update("UPDATE USUARIO SET PODE_CRIAR_PLANEJAMENTO=:create,PODE_EDITAR_PLANEJAMENTO=:edit,PODE_ADICIONAR_VISITAS=COALESCE(:addVisits,PODE_ADICIONAR_VISITAS),PODE_REORDENAR_VISITAS=COALESCE(:reorderVisits,PODE_REORDENAR_VISITAS) WHERE ID=:id AND ID_CEDE=:cede AND SITUACAO=1 AND SITUACAO_APROVACAO=1",new org.springframework.jdbc.core.namedparam.MapSqlParameterSource("id",id).addValue("cede",CurrentUser.cede()).addValue("create",value.createPlanning()).addValue("edit",value.editPlanning()).addValue("addVisits",value.addVisits()).addValue("reorderVisits",value.reorderVisits()));if(n!=1)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);}
 @GetMapping("/sede-info") public Map<String,Object> sede(){return db.queryForMap("SELECT ID AS id,NOME AS nome,CODIGO AS codigo FROM CEDE WHERE ID=:id",Map.of("id",CurrentUser.cede()));}
}
