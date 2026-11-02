package com.inteliagent.auth.repository;
import org.springframework.stereotype.Repository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Value;
@Repository
public class UsuarioRepository {
    private final JavaMailSender sender;
    private final String from;
    public UsuarioRepository(JavaMailSender sender, @Value("${mail.from}") String from) { this.sender = sender; this.from = from; }
    public void sendEmail(String email, String subject, String text) {
        var message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(email); message.setSubject(subject); message.setText(text.replaceAll("<[^>]*>", ""));
        sender.send(message);
    }
}
