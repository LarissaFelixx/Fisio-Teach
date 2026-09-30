package com.app.fisiotech.auth;

import com.app.fisiotech.auth.service.EnvioCodigoRecuperacaoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class RecuperacaoSenhaIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    // Substitui o envio real para o teste conseguir ler o código gerado.
    @MockitoBean EnvioCodigoRecuperacaoService envioCodigo;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String auth, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (auth != null) builder.header("Authorization", auth);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> login(String email, String senha) throws Exception {
        return request("POST", "/auth/login", null, Map.of("email", email, "senha", senha));
    }

    @Test
    void redefinirSenhaTrocaASenhaERevogaAsSessoesAbertas() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        assertThat(request("POST", "/pacientes/cadastro", null,
                Map.of("nome", "Paciente", "email", email, "senha", "senha12345")).statusCode()).isEqualTo(201);
        var tokens = json.readTree(login(email, "senha12345").body());
        String access = "Bearer " + tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();

        assertThat(request("POST", "/auth/recuperar-senha", null, Map.of("email", email)).statusCode()).isEqualTo(204);
        ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);
        verify(envioCodigo).enviar(eq(email), codigo.capture());

        assertThat(request("POST", "/auth/redefinir-senha", null,
                Map.of("email", email, "codigo", codigo.getValue(), "novaSenha", "novaSenha123")).statusCode()).isEqualTo(204);

        assertThat(request("GET", "/auth/me", access, null).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/auth/refresh", null, Map.of("refreshToken", refresh)).statusCode()).isEqualTo(401);
        assertThat(login(email, "senha12345").statusCode()).isEqualTo(401);
        assertThat(login(email, "novaSenha123").statusCode()).isEqualTo(200);
    }

    @Test
    void endpointsPublicosRespondemSemRevelarSeOEmailExiste() throws Exception {
        assertThat(request("POST", "/auth/recuperar-senha", null,
                Map.of("email", "ninguem@example.com")).statusCode()).isEqualTo(204);

        var resposta = request("POST", "/auth/redefinir-senha", null,
                Map.of("email", "ninguem@example.com", "codigo", "123456", "novaSenha", "novaSenha123"));
        assertThat(resposta.statusCode()).isEqualTo(400);
        assertThat(resposta.body()).contains("Código inválido ou expirado.");
    }

    @Test
    void corpoInvalidoRetorna400EmVezDe401() throws Exception {
        assertThat(request("POST", "/auth/redefinir-senha", null,
                Map.of("email", "x", "codigo", "12", "novaSenha", "a")).statusCode()).isEqualTo(400);
    }
}
