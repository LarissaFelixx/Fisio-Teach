package com.app.fisiotech.auth;

import com.app.fisiotech.auth.repository.*;
import com.app.fisiotech.auth.entity.AuthSession;
import com.app.fisiotech.auth.config.EmailRegistryInitializer;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class AuthIntegrationTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper json;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired AuthSessionRepository sessions;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PacienteRepository pacientes;
    @Autowired ProfissionalRepository profissionais;
    @Autowired EmailRegistryInitializer registryInitializer;
    @Autowired com.app.fisiotech.admin.service.AdminService adminService;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String auth, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (auth != null) builder.header("Authorization", auth);
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Map<String, String> login(String email, String password) throws Exception {
        var response = request("POST", "/auth/login", null, Map.of("email", email, "senha", password));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Cache-Control")).hasValue("no-store");
        return tokens(response);
    }

    private Map<String, String> tokens(HttpResponse<String> response) throws Exception {
        var node = json.readTree(response.body());
        return Map.of("access", node.get("accessToken").asText(), "refresh", node.get("refreshToken").asText());
    }

    private String patient() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        assertThat(request("POST", "/pacientes/cadastro", null,
                Map.of("nome", "Paciente", "email", email, "senha", "senha12345")).statusCode()).isEqualTo(201);
        return email;
    }

    private String professional() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        var admin = login("admin@fisiotech.com", "12345678");
        assertThat(request("POST", "/profissionais", "Bearer " + admin.get("access"),
                Map.of("nome", "Profissional", "email", email, "senha", "senha12345",
                        "registroProfissional", UUID.randomUUID().toString().substring(0, 20),
                        "especialidade", "Ortopedia")).statusCode()).isEqualTo(201);
        return email;
    }

    private HttpResponse<String> refresh(String token) throws Exception {
        return request("POST", "/auth/refresh", null, Map.of("refreshToken", token));
    }

    @Test void loginRolesAndExistingPrincipalContract() throws Exception {
        var accounts = Map.of("admin@fisiotech.com", "ADMIN", patient(), "PACIENTE", professional(), "PROFISSIONAL");
        for (var account : accounts.entrySet()) {
            var tokens = login(account.getKey(), account.getValue().equals("ADMIN") ? "12345678" : "senha12345");
            var me = request("GET", "/auth/me", "Bearer " + tokens.get("access"), null);
            assertThat(me.statusCode()).isEqualTo(200);
            assertThat(json.readTree(me.body()).get("role").asText()).isEqualTo("ROLE_" + account.getValue());
            assertThat(json.readTree(me.body()).get("email").asText()).isEqualTo(account.getKey());
            assertThat(me.body()).doesNotContain("senha", "password");
            assertThat(decoder.decode(tokens.get("access")).getSubject()).startsWith(account.getValue() + ":");
        }
    }

    @Test void rejectsBadCredentialsBasicAndMissingAuthentication() throws Exception {
        assertThat(request("POST", "/auth/login", null, Map.of("email", "admin@fisiotech.com", "senha", "wrong")).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/auth/login", null, Map.of("email", "absent@example.com", "senha", "wrong")).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/auth/login", null, Map.of("email", "invalid", "senha", "")).statusCode()).isEqualTo(400);
        assertThat(request("GET", "/auth/me", null, null).statusCode()).isEqualTo(401);
        String basic = Base64.getEncoder().encodeToString("admin@fisiotech.com:12345678".getBytes());
        assertThat(request("GET", "/auth/me", "Basic " + basic, null).statusCode()).isEqualTo(401);
    }

    @Test void preservesRoleRestrictionsAndPatientIsolation() throws Exception {
        var patient = login(patient(), "senha12345");
        var professional = login(professional(), "senha12345");
        assertThat(request("GET", "/profissionais", "Bearer " + patient.get("access"), null).statusCode()).isEqualTo(403);
        assertThat(request("GET", "/me", "Bearer " + professional.get("access"), null).statusCode()).isEqualTo(403);
        assertThat(request("GET", "/me", "Bearer " + patient.get("access"), null).statusCode()).isEqualTo(200);
        Long patientId = Long.valueOf(decoder.decode(patient.get("access")).getSubject().split(":")[1]);
        assertThat(request("GET", "/pacientes/" + patientId, "Bearer " + professional.get("access"), null).statusCode()).isEqualTo(404);
    }

    @Test void rotationAndReplayRevokeWholeSession() throws Exception {
        var initial = login(patient(), "senha12345");
        var rotated = refresh(initial.get("refresh"));
        assertThat(rotated.statusCode()).isEqualTo(200);
        var next = tokens(rotated);
        assertThat(next.get("refresh")).isNotEqualTo(initial.get("refresh"));
        assertThat(refreshTokens.findAll()).noneMatch(t -> t.getTokenHash().equals(initial.get("refresh")));
        assertThat(refresh(initial.get("refresh")).statusCode()).isEqualTo(401);
        assertThat(refresh(next.get("refresh")).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/auth/me", "Bearer " + next.get("access"), null).statusCode()).isEqualTo(401);
    }

    @Test void concurrentRefreshAllowsOnlyOneRotationAndRevokesOnReplay() throws Exception {
        var initial = login(patient(), "senha12345");
        var gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<HttpResponse<String>> call = () -> { gate.await(); return refresh(initial.get("refresh")); };
            var a = executor.submit(call);
            var b = executor.submit(call);
            gate.countDown();
            var first = a.get(20, TimeUnit.SECONDS);
            var second = b.get(20, TimeUnit.SECONDS);
            assertThat(List.of(first.statusCode(), second.statusCode())).containsExactlyInAnyOrder(200, 401);
            var success = tokens(first.statusCode() == 200 ? first : second);
            assertThat(refresh(success.get("refresh")).statusCode()).isEqualTo(401);
        }
    }

    @Test void logoutRevokesOnlyItsSessionAndIsIdempotent() throws Exception {
        String email = patient();
        var a = login(email, "senha12345");
        var b = login(email, "senha12345");
        for (int i = 0; i < 2; i++) assertThat(request("POST", "/auth/logout", null,
                Map.of("refreshToken", a.get("refresh"))).statusCode()).isEqualTo(204);
        assertThat(request("GET", "/auth/me", "Bearer " + a.get("access"), null).statusCode()).isEqualTo(401);
        assertThat(refresh(a.get("refresh")).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/auth/me", "Bearer " + b.get("access"), null).statusCode()).isEqualTo(200);
        assertThat(refresh("unknown").statusCode()).isEqualTo(401);
    }

    @Test void passwordChangeRevokesAllSessions() throws Exception {
        String email = patient();
        var a = login(email, "senha12345");
        var b = login(email, "senha12345");
        assertThat(request("PUT", "/me/senha", "Bearer " + a.get("access"),
                Map.of("senhaAtual", "senha12345", "novaSenha", "novaSenha12345")).statusCode()).isEqualTo(204);
        assertThat(request("GET", "/auth/me", "Bearer " + a.get("access"), null).statusCode()).isEqualTo(401);
        assertThat(refresh(b.get("refresh")).statusCode()).isEqualTo(401);
        assertThat(login(email, "novaSenha12345")).containsKeys("access", "refresh");
    }

    @Test void deletedAccountCannotUseExistingTokens() throws Exception {
        String email = professional();
        var tokens = login(email, "senha12345");
        var admin = login("admin@fisiotech.com", "12345678");
        long id = profissionais.findByEmail(email).orElseThrow().getId();
        assertThat(request("DELETE", "/profissionais/" + id, "Bearer " + admin.get("access"), null).statusCode()).isEqualTo(204);
        assertThat(request("GET", "/auth/me", "Bearer " + tokens.get("access"), null).statusCode()).isEqualTo(401);
        assertThat(refresh(tokens.get("refresh")).statusCode()).isEqualTo(401);
    }

    @Test void adminAndProfessionalPasswordChangesRevokeAccess() throws Exception {
        String adminEmail = UUID.randomUUID() + "@example.com";
        adminService.criar(new com.app.fisiotech.admin.dto.AdminCreateRequest("Admin", adminEmail, "senha12345"));
        for (var entry : Map.of(adminEmail, "/admin/me/senha", professional(), "/profissionais/me/senha").entrySet()) {
            var tokens = login(entry.getKey(), "senha12345");
            assertThat(request("PUT", entry.getValue(), "Bearer " + tokens.get("access"),
                    Map.of("senhaAtual", "senha12345", "novaSenha", "novaSenha12345")).statusCode()).isEqualTo(204);
            assertThat(request("GET", "/auth/me", "Bearer " + tokens.get("access"), null).statusCode()).isEqualTo(401);
            assertThat(refresh(tokens.get("refresh")).statusCode()).isEqualTo(401);
            login(entry.getKey(), "novaSenha12345");
        }
    }

    @Test void administrativePasswordResetsRevokePatientAndProfessionalSessions() throws Exception {
        var admin = login("admin@fisiotech.com", "12345678");
        String professionalEmail = professional();
        var professional = profissionais.findByEmail(professionalEmail).orElseThrow();
        var professionalTokens = login(professionalEmail, "senha12345");
        String patientEmail = patient();
        var patient = pacientes.findByEmail(patientEmail).orElseThrow();
        var patientTokens = login(patientEmail, "senha12345");
        assertThat(request("PUT", "/admin/pacientes/" + patient.getId(), "Bearer " + admin.get("access"),
                Map.of("nome", "Paciente", "email", patientEmail, "senha", "redefinida123",
                        "profissionalId", professional.getId())).statusCode()).isEqualTo(204);
        assertThat(request("PUT", "/profissionais/" + professional.getId(), "Bearer " + admin.get("access"),
                Map.of("nome", "Profissional", "email", professionalEmail, "senha", "redefinida123",
                        "registroProfissional", professional.getRegistroProfissional(), "especialidade", "Ortopedia")).statusCode()).isEqualTo(204);
        for (var tokens : List.of(patientTokens, professionalTokens)) {
            assertThat(request("GET", "/auth/me", "Bearer " + tokens.get("access"), null).statusCode()).isEqualTo(401);
            assertThat(refresh(tokens.get("refresh")).statusCode()).isEqualTo(401);
        }
        login(patientEmail, "redefinida123");
        login(professionalEmail, "redefinida123");
    }

    @Test void swaggerDocumentsBearerAndPublicLogin() throws Exception {
        var response = request("GET", "/v3/api-docs", null, null);
        assertThat(response.statusCode()).isEqualTo(200);
        var spec = json.readTree(response.body());
        assertThat(spec.at("/components/securitySchemes/bearerAuth/scheme").asText()).isEqualTo("bearer");
        assertThat(spec.at("/paths/~1auth~1login/post/security").isEmpty()).isTrue();
    }

    @Test void globalEmailUniquenessRollsBackCreationAndUpdate() throws Exception {
        assertThat(request("POST", "/pacientes/cadastro", null,
                Map.of("nome", "Paciente", "email", "ADMIN@fisiotech.com", "senha", "senha12345")).statusCode()).isEqualTo(409);
        assertThat(pacientes.findByEmail("admin@fisiotech.com")).isEmpty();
        String email = patient();
        var tokens = login(email, "senha12345");
        assertThat(request("PUT", "/me", "Bearer " + tokens.get("access"),
                Map.of("nome", "Paciente", "email", "admin@fisiotech.com")).statusCode()).isEqualTo(409);
        assertThat(pacientes.findByEmail(email)).isPresent();
    }

    @Test void legacyEmailConflictFailsRegistryInitialization() throws Exception {
        String email = patient();
        var duplicate = profissionais.save(new Profissional("Duplicado", email, "hash", UUID.randomUUID().toString().substring(0, 20), "Teste"));
        try {
            assertThatThrownBy(() -> registryInitializer.run(null))
                    .isInstanceOf(com.app.fisiotech.exception.EmailJaCadastradoException.class);
        } finally { profissionais.deleteById(duplicate.getId()); }
    }

    @Test void rejectsExpiredSessionEvenWithValidSignature() throws Exception {
        var tokens = login(patient(), "senha12345");
        var jwt = decoder.decode(tokens.get("access"));
        var old = sessions.findById(jwt.getClaimAsString("sid")).orElseThrow();
        sessions.save(new AuthSession(old.getId(), old.getSubject(), old.getCredentialHash(), Instant.now().minusSeconds(1)));
        assertThat(request("GET", "/auth/me", "Bearer " + tokens.get("access"), null).statusCode()).isEqualTo(401);
        assertThat(refresh(tokens.get("refresh")).statusCode()).isEqualTo(401);
    }

    @Test void validatesSignatureExpiryIssuerAudienceRoleAndSubject() throws Exception {
        var tokens = login(patient(), "senha12345");
        var jwt = decoder.decode(tokens.get("access"));
        for (String field : List.of("iss", "aud", "role", "sub", "sid", "exp")) {
            var builder = JwtClaimsSet.builder().claims(claims -> claims.putAll(jwt.getClaims()));
            switch (field) {
                case "iss" -> builder.issuer("wrong");
                case "aud" -> builder.audience(List.of("wrong"));
                case "role" -> builder.claim("role", "ROLE_ADMIN");
                case "sub" -> builder.subject("ADMIN:1");
                case "sid" -> builder.claim("sid", UUID.randomUUID().toString());
                case "exp" -> builder.issuedAt(Instant.now().minusSeconds(60)).expiresAt(Instant.now().minusSeconds(1));
            }
            String signed = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), builder.build())).getTokenValue();
            assertThat(request("GET", "/auth/me", "Bearer " + signed, null).statusCode()).as(field).isEqualTo(401);
        }
        String[] pieces = tokens.get("access").split("\\.");
        String tampered = pieces[0] + "." + pieces[1] + "." + (pieces[2].charAt(0) == 'A' ? "B" : "A") + pieces[2].substring(1);
        assertThat(request("GET", "/auth/me", "Bearer " + tampered, null).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/auth/me", "Bearer malformed", null).statusCode()).isEqualTo(401);
    }

    @Test void corsAllowsMobileAuthorizationHeader() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/me"))
                .header("Origin", "capacitor://localhost")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).hasValue("capacitor://localhost");
    }
}
