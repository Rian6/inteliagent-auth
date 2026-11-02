package com.inteliagent.auth.persistence;

import com.inteliagent.auth.entity.Usuario;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UsuarioPersistence {
    public UsuarioPersistence(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void updatePassword(int id, byte[] password) {
        jdbcTemplate.update("UPDATE usuario SET senha = :senha WHERE id = :id", new MapSqlParameterSource("id", id).addValue("senha", password));
    }
    public Usuario findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = :email";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("email", email), new UsuarioRowMapper());
    }

    public Usuario login(String email) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("email", email);

        String sql = "SELECT * FROM usuario WHERE email = :email";

        return jdbcTemplate.query(sql, paramSource, new LoginRowMapper()).stream().findFirst().orElse(null);
    }

    public void save(Usuario usuario) throws Exception{
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cede WHERE id=:id AND situacao=1", new MapSqlParameterSource("id", usuario.getIdCede()), Integer.class) != 1) throw new IllegalArgumentException("Sede inválida");
        String sql = "INSERT INTO usuario (nome, email, senha, id_cede, codigo_email, id_imagem_perfil_usuario, situacao_aprovacao) SELECT :nome, :email, :senha, :idCede, 1, :idImagemPerfilUsuario, CASE WHEN AUTO_APROVAR=1 THEN 1 ELSE 0 END FROM cede WHERE ID=:idCede AND SITUACAO=1";

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("nome", usuario.getNome());
        paramSource.addValue("email", usuario.getEmail());
        paramSource.addValue("senha", usuario.getSecurityPassword());
        paramSource.addValue("idCede", usuario.getIdCede());
        paramSource.addValue("idImagemPerfilUsuario", usuario.getIdImagemPerfilUsuario() == 0 ? null : usuario.getIdImagemPerfilUsuario());

        jdbcTemplate.update(sql, paramSource);
    }

    private static final class UsuarioRowMapper implements RowMapper<Usuario> {
        @Override
        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
            Usuario usuario = new Usuario();
            usuario.setId(rs.getInt("id"));
            usuario.setNome(rs.getString("nome"));
            usuario.setEmail(rs.getString("email"));
            usuario.setIdCede(rs.getInt("id_cede"));
            usuario.setSituacaoAprovacao(rs.getInt("situacao_aprovacao"));
            usuario.setCodigoEmail(rs.getString("codigo_email"));
            usuario.setSituacao(rs.getInt("situacao"));
            return usuario;
        }
    }

    private static final class LoginRowMapper implements RowMapper<Usuario> {
        @Override
        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
            Usuario usuario = new Usuario();
            usuario.setId(rs.getInt("id"));
            usuario.setNome(rs.getString("nome"));
            usuario.setEmail(rs.getString("email"));
            usuario.setSecurityPassword(rs.getBytes("senha"));
            usuario.setRole(rs.getString("role"));
            usuario.setPermissions(java.util.Map.of("createPlanning",rs.getBoolean("PODE_CRIAR_PLANEJAMENTO"),"editPlanning",rs.getBoolean("PODE_EDITAR_PLANEJAMENTO"),"addVisits",rs.getBoolean("PODE_ADICIONAR_VISITAS"),"reorderVisits",rs.getBoolean("PODE_REORDENAR_VISITAS")));
            usuario.setTokenVersion(rs.getInt("token_version"));
            usuario.setIdCede(rs.getInt("id_cede"));
            usuario.setSituacaoAprovacao(rs.getInt("situacao_aprovacao"));
            usuario.setCodigoEmail(rs.getString("codigo_email"));
            usuario.setSituacao(rs.getInt("situacao"));
            return usuario;
        }
    }
}
