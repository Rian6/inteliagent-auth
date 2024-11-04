package com.inteliagent.auth.persistence;

import com.inteliagent.auth.entity.EmailLiberacao;
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
public class EmailLiberacaoPersistence {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void save(EmailLiberacao emailLiberacao) throws Exception{
        String sql = "REPLACE INTO email_liberacao (email, codigo) VALUES (:email, :codigo)";

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("email", emailLiberacao.getEmail());
        paramSource.addValue("codigo", emailLiberacao.getCodigo());

        jdbcTemplate.update(sql, paramSource);
    }

    public boolean findByEmail(String email, String codigo) {
        String sql = """
                SELECT el.id FROM email_liberacao el
                WHERE el.codigo = :codigo
                AND el.email = :email""";

        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("email", email);
        mapSqlParameterSource.addValue("codigo", Integer.parseInt(codigo));

        return !jdbcTemplate.query(
                sql,
                mapSqlParameterSource,
                new EmailLiberacaoPersistence.EmailLiberacaoRowMapper())
                .isEmpty();
    }

    private static final class EmailLiberacaoRowMapper implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
