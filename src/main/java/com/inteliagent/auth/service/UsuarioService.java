package com.inteliagent.auth.service;

import com.inteliagent.auth.entity.EmailLiberacao;
import com.inteliagent.auth.entity.ImagemPerfilUsuario;
import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.persistence.EmailLiberacaoPersistence;
import com.inteliagent.auth.persistence.ImagemPerfilUsuarioPersistence;
import com.inteliagent.auth.persistence.UsuarioPersistence;
import com.inteliagent.auth.util.EncodeUtil;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.security.SecureRandom;

@Service
public class UsuarioService {
    private static final byte[] DUMMY_PASSWORD = EncodeUtil.upgrade("invalid-credentials-dummy");
    public UsuarioService(UsuarioPersistence usuarioPersistence, EmailLiberacaoPersistence emailLiberacaoPersistence, ImagemPerfilUsuarioPersistence imagemPerfilUsuarioPersistence, AuthMailOutbox mailOutbox) {
        this.usuarioPersistence = usuarioPersistence;
        this.emailLiberacaoPersistence = emailLiberacaoPersistence;
        this.imagemPerfilUsuarioPersistence = imagemPerfilUsuarioPersistence;
        this.mailOutbox = mailOutbox;
    }


    private final UsuarioPersistence usuarioPersistence;


    private final EmailLiberacaoPersistence emailLiberacaoPersistence;

    private final ImagemPerfilUsuarioPersistence imagemPerfilUsuarioPersistence;
    private final AuthMailOutbox mailOutbox;

    public void validarUsuario(Usuario usuario){
        usuarioPersistence.findByEmail(usuario.getEmail());
    }

    public Usuario validateCodeUser(Usuario usuario){
        boolean isValid = emailLiberacaoPersistence.findByEmail(usuario.getEmail(), usuario.getCodigoEmail());

        if(!isValid){
            return null;
        }

        return usuario;
    }


    public Usuario logar(Usuario usuario) throws Exception {
        Usuario usuarioBanco = usuarioPersistence.login(usuario.getEmail());

        if (usuarioBanco == null) {
            EncodeUtil.matches(usuario.getSenha(), DUMMY_PASSWORD);
            return null;
        }



        if (!EncodeUtil.matches(usuario.getSenha(), usuarioBanco.getSecurityPassword())) {
            return null;
        }
        if (usuarioBanco.getSituacao() != 1 || usuarioBanco.getSituacaoAprovacao() != 1) return null;
        if (EncodeUtil.legacy(usuarioBanco.getSecurityPassword())) usuarioPersistence.updatePassword(usuarioBanco.getId(), EncodeUtil.upgrade(usuario.getSenha()));
        usuarioBanco.setSecurityPassword(null);
        usuarioBanco.setSenha(null);

        return usuarioBanco;
    }


    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void criarUsuario(Usuario usuario) throws Exception {
        if (usuario.getNome() == null || usuario.getNome().isBlank() || usuario.getNome().length() > 255 || usuario.getIdCede() < 1) throw new IllegalArgumentException("Dados inválidos");
        if (!emailLiberacaoPersistence.consume(usuario.getEmail(), usuario.getCodigoEmail())) throw new IllegalArgumentException("Código inválido ou expirado");
        ImagemPerfilUsuario imagemPerfilUsuario = usuario.getImagemPerfilUsuario();
        if (imagemPerfilUsuario != null && imagemPerfilUsuario.getImageDataBase64() != null) {
        String imageBase64 = imagemPerfilUsuario.getImageDataBase64();
        if (imageBase64.length() > 1_400_000) throw new IllegalArgumentException("Imagem muito grande");
        byte[] imageBlob = Base64.getDecoder().decode(imageBase64);
        imagemPerfilUsuario.setImageData(imageBlob);

        int idImagemPerfil = imagemPerfilUsuarioPersistence.save(imagemPerfilUsuario);
        usuario.setIdImagemPerfilUsuario(idImagemPerfil);
        }

        byte[] passwordEncyper = EncodeUtil.hash(usuario.getSenha());
        usuario.setSecurityPassword(passwordEncyper);

        usuarioPersistence.save(usuario);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void sendEmail(String email) throws Exception {
        if (email == null || email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Email inválido");
        String code = generateCode();
        EmailLiberacao emailLiberacao = new EmailLiberacao();
        emailLiberacao.setEmail(email);
        emailLiberacao.setCodigo(code);
        emailLiberacaoPersistence.save(emailLiberacao);

        mailOutbox.enqueue(email,"REGISTER",code);
    }

    private String generateCode(){
        SecureRandom random = new SecureRandom();
        return String.valueOf((100000 + random.nextInt(900000)));
    }
}
