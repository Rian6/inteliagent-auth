package com.inteliagent.auth.persistence;

import com.inteliagent.auth.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CedePersistence {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Usuario> findUsuariosCede(Integer codigo) {
        String sql = "SELECT c.id FROM cede c\n" +
                "where c.codigo = :codigo";

        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("codigo", codigo);

        return jdbcTemplate.query(sql, mapSqlParameterSource, new UsuarioRowMapper());
    }

    private static final class UsuarioRowMapper implements RowMapper<Usuario> {
        @Override
        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
            Usuario usuario = new Usuario();
            usuario.setId(rs.getInt("id"));
            return usuario;
        }
    }
}
