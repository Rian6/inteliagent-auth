package com.inteliagent.auth.controller.socket;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inteliagent.auth.dto.auth.Solicitacao;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CedeValidatorSocketController extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Nova conexão estabelecida: " + session.getId());
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Payload=" + payload);

        ObjectMapper objectMapper = new ObjectMapper();
        Solicitacao solicitacao = objectMapper.readValue(payload, Solicitacao.class);

        if (solicitacao.getSituacao().equals("aprovado")) {
            //alterar notificação para o site
            notificarAprovacao("aprovado", solicitacao.getIdCede());
        } else if (solicitacao.getSituacao().equals("rejeitado")) {
            //alterar notificação para o site
            notificarAprovacao("regeitado", solicitacao.getIdCede());
        } else if (solicitacao.getSituacao().equals("request")) {
            //inserir notificação para o site
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Conexão fechada: " + session.getId());
        sessions.remove(session);
    }

    private void notificarAprovacao(String status, int idCede) {
        sessions.forEach(session -> {
            try {
                session.sendMessage(new TextMessage("{\"idCede\": " + idCede + ", \"status\": \"" + status + "\"}"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
