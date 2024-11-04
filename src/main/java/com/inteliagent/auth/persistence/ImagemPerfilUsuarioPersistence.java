package com.inteliagent.auth.persistence;

import com.inteliagent.auth.entity.ImagemPerfilUsuario;
import com.inteliagent.auth.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ImagemPerfilUsuarioPersistence {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public int save(ImagemPerfilUsuario imagemPerfilUsuario) throws Exception{

        String sql = """
                INSERT INTO IMAGEM_PERFIL_USUARIO (image_data, image_name, image_type)
                VALUES(:IMAGEDATA, :IMAGENAME, :IMAGETYPE)""";

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        paramSource.addValue("IMAGEDATA", imagemPerfilUsuario.getImageData());
        paramSource.addValue("IMAGENAME", imagemPerfilUsuario.getImageName());
        paramSource.addValue("IMAGETYPE", imagemPerfilUsuario.getImageType());

        return jdbcTemplate.update(sql, paramSource);
    }
}
