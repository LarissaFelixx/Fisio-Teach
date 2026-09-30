package com.app.fisiotech;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class BusinessEndpointsIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String token, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String email, String senha) throws Exception {
        var response = request("POST", "/auth/login", null, Map.of("email", email, "senha", senha));
        assertThat(response.statusCode()).isEqualTo(200);
        return json.readTree(response.body()).get("accessToken").asText();
    }

    @Test
    void percorreCadastroPaginacaoConsultaAgendaEIndicadores() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String profissionalEmail = "fisio-" + suffix + "@example.com";
        String admin = login("admin@fisiotech.com", "12345678");
        var profissional = request("POST", "/profissionais", admin, Map.of(
                "nome", "Fisio Integração", "email", profissionalEmail, "senha", "senha12345",
                "registroProfissional", "CREFITO-" + suffix, "especialidade", "Ortopedia"));
        assertThat(profissional.statusCode()).isEqualTo(201);

        String token = login(profissionalEmail, "senha12345");
        String pacienteEmail = "paciente-" + suffix + "@example.com";
        var paciente = request("POST", "/pacientes", token,
                Map.of("nome", "Paciente Integração", "email", pacienteEmail, "senha", "senha12345"));
        assertThat(paciente.statusCode()).isEqualTo(201);
        long pacienteId = Long.parseLong(paciente.headers().firstValue("Location").orElseThrow().replaceAll(".*/", ""));

        var pagina = request("GET", "/pacientes/paginado?filtro=Integra%C3%A7%C3%A3o&page=0&size=1&sort=nome", token, null);
        assertThat(pagina.statusCode()).isEqualTo(200);
        assertThat(json.readTree(pagina.body()).get("content").size()).isEqualTo(1);
        assertThat(json.readTree(pagina.body()).get("totalElements").asLong()).isGreaterThanOrEqualTo(1);

        LocalDateTime horario = LocalDateTime.now().plusDays(5).withSecond(0).withNano(0);
        var consulta = request("POST", "/consultas", token, Map.of(
                "pacienteId", pacienteId, "dataHora", horario.toString(), "tipo", "PRESENCIAL", "valor", 150));
        assertThat(consulta.statusCode()).isEqualTo(201);

        String inicio = horario.minusHours(1).toString().replace(":", "%3A");
        String fim = horario.plusHours(1).toString().replace(":", "%3A");
        var agenda = request("GET", "/agenda?inicio=" + inicio + "&fim=" + fim, token, null);
        assertThat(agenda.statusCode()).isEqualTo(200);
        assertThat(json.readTree(agenda.body()).size()).isEqualTo(1);
        var indicadores = request("GET", "/agenda/indicadores?inicio=" + inicio + "&fim=" + fim, token, null);
        assertThat(indicadores.statusCode()).isEqualTo(200);
        assertThat(json.readTree(indicadores.body()).get("totalConsultas").asLong()).isEqualTo(1);
    }

    @Test
    void impedePacienteDeAcessarRotasProfissionais() throws Exception {
        String email = "isolado-" + UUID.randomUUID() + "@example.com";
        assertThat(request("POST", "/pacientes/cadastro", null,
                Map.of("nome", "Paciente", "email", email, "senha", "senha12345")).statusCode()).isEqualTo(201);
        String token = login(email, "senha12345");
        assertThat(request("GET", "/agenda?inicio=2030-01-01T00%3A00&fim=2030-01-02T00%3A00", token, null).statusCode())
                .isEqualTo(403);
        assertThat(request("GET", "/pacientes/paginado", token, null).statusCode()).isEqualTo(403);
    }
}
