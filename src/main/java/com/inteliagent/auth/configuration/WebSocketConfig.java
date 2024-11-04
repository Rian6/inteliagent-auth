package com.inteliagent.auth.configuration;

import com.inteliagent.auth.controller.socket.CedeValidatorSocketController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CedeValidatorSocketController cedeValidatorSocketController;

    public WebSocketConfig(CedeValidatorSocketController cedeValidatorSocketController) {
        this.cedeValidatorSocketController = cedeValidatorSocketController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cedeValidatorSocketController, "/aprove").setAllowedOrigins("*");
    }
}
