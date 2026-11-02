package com.inteliagent.auth.controller.http;

import com.inteliagent.auth.configuration.CurrentUser;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user/me")
public class PerfilController {
    private final JdbcTemplate db;
    public PerfilController(JdbcTemplate db) { this.db = db; }

    @GetMapping
    public ResponseEntity<Map<String,Object>> profile() {
        var result = db.queryForMap("SELECT u.id,u.nome,u.email,u.role,u.id_cede AS idCede,u.situacao_aprovacao AS situacaoAprovacao,c.codigo AS codigoSede,c.nome AS nomeSede,i.image_data,i.image_type FROM usuario u JOIN cede c ON c.id=u.id_cede LEFT JOIN imagem_perfil_usuario i ON i.id=u.id_imagem_perfil_usuario WHERE u.id=? AND u.id_cede=?",CurrentUser.id(),CurrentUser.cede());
        byte[] photo = (byte[])result.remove("image_data");
        String mime = (String)result.remove("image_type");
        result.put("foto",photo!=null && Set.of("image/jpeg","image/png").contains(mime==null?"":mime) ? "data:"+mime+";base64,"+Base64.getEncoder().encodeToString(photo) : null);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @PutMapping("/photo")
    @Transactional
    public ResponseEntity<Map<String,Object>> photo(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize()>1500000) throw invalid("Escolha uma imagem de até 1,5 MB.");
        byte[] normalized;
        try(var input=ImageIO.createImageInputStream(file.getInputStream())) {
            var readers=ImageIO.getImageReaders(input);
            if(!readers.hasNext()) throw invalid("Escolha uma imagem JPEG ou PNG válida.");
            var reader=readers.next();
            try {
                reader.setInput(input);
                String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!Set.of("jpeg","jpg","png").contains(format)) throw invalid("Escolha uma imagem JPEG ou PNG válida.");
                int w=reader.getWidth(0), h=reader.getHeight(0);
                if(w<1 || h<1 || (long)w*h>16000000) throw invalid("A imagem é muito grande. Escolha uma foto menor.");
                var source=reader.read(0);
                int side=Math.min(w,h);
                var output=new BufferedImage(512,512,BufferedImage.TYPE_INT_RGB);
                var graphics=output.createGraphics();
                try {
                    graphics.setColor(java.awt.Color.WHITE); graphics.fillRect(0,0,512,512);
                    graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    graphics.drawImage(source,0,0,512,512,(w-side)/2,(h-side)/2,(w+side)/2,(h+side)/2,null);
                } finally { graphics.dispose(); }
                var bytes=new ByteArrayOutputStream(); ImageIO.write(output,"jpeg",bytes); normalized=bytes.toByteArray();
            } finally { reader.dispose(); }
        } catch(ResponseStatusException error) { throw error; }
          catch(Exception error) { throw invalid("Não foi possível abrir a imagem. Escolha outra foto."); }
        var owner=db.queryForMap("SELECT id_imagem_perfil_usuario FROM usuario WHERE id=? FOR UPDATE",CurrentUser.id());
        Number old=(Number)owner.get("id_imagem_perfil_usuario");
        var key=new GeneratedKeyHolder();
        db.update(connection->{var statement=connection.prepareStatement("INSERT INTO imagem_perfil_usuario(image_data,image_name,image_type) VALUES (?,'perfil.jpg','image/jpeg')",java.sql.Statement.RETURN_GENERATED_KEYS);statement.setBytes(1,normalized);return statement;},key);
        db.update("UPDATE usuario SET id_imagem_perfil_usuario=? WHERE id=?",key.getKey().intValue(),CurrentUser.id());
        if(old!=null) db.update("DELETE FROM imagem_perfil_usuario WHERE id=? AND NOT EXISTS(SELECT 1 FROM usuario WHERE id_imagem_perfil_usuario=?)",old.intValue(),old.intValue());
        return profile();
    }

    @DeleteMapping("/photo")
    @Transactional
    public ResponseEntity<Map<String,Object>> remove() {
        var owner=db.queryForMap("SELECT id_imagem_perfil_usuario FROM usuario WHERE id=? FOR UPDATE",CurrentUser.id());
        Number old=(Number)owner.get("id_imagem_perfil_usuario");
        db.update("UPDATE usuario SET id_imagem_perfil_usuario=NULL WHERE id=?",CurrentUser.id());
        if(old!=null) db.update("DELETE FROM imagem_perfil_usuario WHERE id=? AND NOT EXISTS(SELECT 1 FROM usuario WHERE id_imagem_perfil_usuario=?)",old.intValue(),old.intValue());
        return profile();
    }
    private ResponseStatusException invalid(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,message); }
}
