package com.inteliagent.auth.persistence;

import com.inteliagent.auth.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UsuarioPersistence {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Usuario findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), new UsuarioRowMapper());
    }

    public Usuario login(String email) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("email", email);

        String sql = "SELECT * FROM usuario WHERE email = :email";

        return jdbcTemplate.queryForObject(sql, paramSource, new LoginRowMapper());
    }

    public void save(Usuario usuario) throws Exception{
        String sql = "INSERT INTO usuario (nome, email, senha, id_cede, codigo_email, id_imagem_perfil_usuario) VALUES (:nome, :email, :senha, :idCede, 1, :idImagemPerfilUsuario)";

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("nome", usuario.getNome());
        paramSource.addValue("email", usuario.getEmail());
        paramSource.addValue("senha", usuario.getSecurityPassword());
        paramSource.addValue("idCede", usuario.getIdCede());
        paramSource.addValue("idImagemPerfilUsuario", usuario.getIdImagemPerfilUsuario());

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
            usuario.setIdCede(rs.getInt("id_cede"));
            usuario.setSituacaoAprovacao(rs.getInt("situacao_aprovacao"));
            usuario.setCodigoEmail(rs.getString("codigo_email"));
            usuario.setSituacao(rs.getInt("situacao"));
            return usuario;
        }
    }
}
