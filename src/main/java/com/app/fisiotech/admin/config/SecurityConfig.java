package com.app.fisiotech.admin.config;

import jakarta.servlet.http.HttpServletResponse;
import com.app.fisiotech.auth.service.AuthService;
import com.app.fisiotech.auth.security.AppUserDetailsService;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService, Environment environment) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").access((authentication, context) -> new AuthorizationDecision(environment.matchesProfiles("dev & !prod")))
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/pacientes/cadastro", "/auth/login", "/auth/refresh", "/auth/logout",
                                "/auth/recuperar-senha", "/auth/redefinir-senha").permitAll()
                        .requestMatchers("/profissionais/me/**").hasRole("PROFISSIONAL")
                        .requestMatchers("/profissionais/**", "/admin/pacientes/**", "/admin/me/**").hasRole("ADMIN")
                        .requestMatchers("/pacientes/**", "/consultas/**", "/mensagens/**", "/avaliacoes/**").hasRole("PROFISSIONAL")
                        .requestMatchers("/me/**").hasRole("PACIENTE")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> writeError(response, 401, "Autenticação inválida ou ausente."))
                        .accessDeniedHandler((request, response, ex) -> writeError(response, 403, "Acesso negado.")))
                .oauth2ResourceServer(resource -> resource
                        .authenticationEntryPoint((request, response, ex) -> writeError(response, 401, "Token inválido ou expirado."))
                        .accessDeniedHandler((request, response, ex) -> writeError(response, 403, "Acesso negado."))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                            var user = authService.authenticate(token);
                            return UsernamePasswordAuthenticationToken
                                    .authenticated(user, null, user.getAuthorities());
                        })));
        return http.build();
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        if (status == 401) response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json;charset=UTF-8");
        String error = status == 401 ? "Unauthorized" : "Forbidden";
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AppUserDetailsService users, PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origens do app empacotado com Capacitor (Android/iOS) e do dev server Angular.
        configuration.setAllowedOrigins(List.of(
                "http://localhost",
                "https://localhost",
                "capacitor://localhost",
                "http://localhost:4200"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
