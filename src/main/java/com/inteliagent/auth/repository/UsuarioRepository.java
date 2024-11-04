package com.inteliagent.auth.repository;

import com.inteliagent.auth.util.EmailUtil;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {
    public void sendEmail(String email, String assunto, String mensagem){
        EmailUtil emailUtil = new EmailUtil();
        emailUtil.sendHtml(email.trim(), assunto, mensagem);
    }
}
