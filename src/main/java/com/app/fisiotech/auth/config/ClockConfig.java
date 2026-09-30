package com.app.fisiotech.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    // Injetar o Clock (em vez de chamar LocalDateTime.now() direto) deixa os testes
    // controlarem o "agora" - essencial para testar a expiração do código.
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

}
