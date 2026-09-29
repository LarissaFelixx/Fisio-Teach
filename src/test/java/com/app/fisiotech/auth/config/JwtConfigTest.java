package com.app.fisiotech.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import java.security.KeyPair;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class JwtConfigTest {
    private final JwtConfig config = new JwtConfig();

    private ByteArrayResource pem(String label, byte[] encoded) {
        return new ByteArrayResource(("-----BEGIN " + label + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
                + "\n-----END " + label + "-----\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test void loadsConfiguredPemKeyPairAndRejectsMismatchedKeys() throws Exception {
        KeyPair keys = config.developmentJwtKeys();
        var pub = pem("PUBLIC KEY", keys.getPublic().getEncoded());
        var priv = pem("PRIVATE KEY", keys.getPrivate().getEncoded());
        var loaded = config.configuredJwtKeys(pub, priv);
        assertThat(loaded.getPublic().getEncoded()).isEqualTo(keys.getPublic().getEncoded());
        var other = config.developmentJwtKeys();
        assertThatThrownBy(() -> config.configuredJwtKeys(pub, pem("PRIVATE KEY", other.getPrivate().getEncoded())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void rejectsMissingRequiredClaimsAndFutureIssuance() throws Exception {
        KeyPair keys = config.developmentJwtKeys();
        Instant now = Instant.parse("2026-09-29T12:00:00Z");
        var decoder = config.jwtDecoder(keys, Clock.fixed(now, ZoneOffset.UTC), "fisiotech", "fisiotech-api");
        var encoder = config.jwtEncoder(keys);
        for (String field : List.of("exp", "iat", "aud", "sub", "jti", "sid", "future")) {
            var builder = JwtClaimsSet.builder().issuer("fisiotech").audience(List.of("fisiotech-api"))
                    .subject("PACIENTE:1").id("id").claim("sid", "session")
                    .issuedAt(now).expiresAt(now.plusSeconds(900));
            if (field.equals("future")) builder.issuedAt(now.plusSeconds(60));
            else builder.claims(claims -> claims.remove(field));
            String signed = encoder.encode(JwtEncoderParameters.from(
                    JwsHeader.with(SignatureAlgorithm.RS256).build(), builder.build())).getTokenValue();
            assertThatThrownBy(() -> decoder.decode(signed)).as(field).isInstanceOf(JwtException.class);
        }
    }
}
