package com.inteliagent.auth.service;

import com.inteliagent.auth.entity.EmailLiberacao;
import com.inteliagent.auth.entity.ImagemPerfilUsuario;
import com.inteliagent.auth.entity.Usuario;
import com.inteliagent.auth.persistence.EmailLiberacaoPersistence;
import com.inteliagent.auth.persistence.ImagemPerfilUsuarioPersistence;
import com.inteliagent.auth.persistence.UsuarioPersistence;
import com.inteliagent.auth.repository.UsuarioRepository;
import com.inteliagent.auth.util.EncodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioPersistence usuarioPersistence;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailLiberacaoPersistence emailLiberacaoPersistence;

    @Autowired
    private ImagemPerfilUsuarioPersistence imagemPerfilUsuarioPersistence;

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
            return null;
        }

        byte[] passwordEncrypt = EncodeUtil.encryptSHA256(usuario.getSenha());

        if (!Arrays.equals(passwordEncrypt, usuarioBanco.getSecurityPassword())) {
            return null;
        }
        usuarioBanco.setSecurityPassword(null);
        usuarioBanco.setSenha(null);

        return usuarioBanco;
    }


    public void criarUsuario(Usuario usuario) throws Exception {
        ImagemPerfilUsuario imagemPerfilUsuario = usuario.getImagemPerfilUsuario();
        String imageBase64 = imagemPerfilUsuario.getImageDataBase64();
        byte[] imageBlob = Base64.getDecoder().decode(imageBase64);
        imagemPerfilUsuario.setImageData(imageBlob);

        int idImagemPerfil = imagemPerfilUsuarioPersistence.save(imagemPerfilUsuario);
        usuario.setIdImagemPerfilUsuario(idImagemPerfil);

        byte[] passwordEncyper = EncodeUtil.encryptSHA256(usuario.getSenha());
        usuario.setSecurityPassword(passwordEncyper);

        usuarioPersistence.save(usuario);
    }

    public void sendEmail(String email) throws Exception {
        String code = generateCode();
        String mensagem = gerarHtmlEmailValidacao();
        mensagem = mensagem.replace("id:codigo", code);

        EmailLiberacao emailLiberacao = new EmailLiberacao();
        emailLiberacao.setEmail(email);
        emailLiberacao.setCodigo(code);
        emailLiberacaoPersistence.save(emailLiberacao);

        usuarioRepository.sendEmail(email, "Código de Verificação", mensagem);
    }

    private String gerarHtmlEmailValidacao() {
        String html = "";
        html = lerArquivo("./modelos/recuperacao/index.html");
        html = html.replace("id:nome-usuario", "teste");

        return html;
    }

    private String lerArquivo(String path){
        StringBuilder html = new StringBuilder();
        try {
            FileReader arq = new FileReader(path);
            BufferedReader lerArq = new BufferedReader(arq);

            String linha = "";
            while (linha != null) {
                linha = lerArq.readLine();
                html.append(linha);
            }
            arq.close();
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
        return html.toString();
    }

    private String generateCode(){
        Random random = new Random();
        return String.valueOf((1000 + random.nextInt(9000)));
    }
}
