package com.app.fisiotech.auth.config;

import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;

@Configuration
public class JwtConfig {
    @Bean public Clock authClock() { return Clock.systemUTC(); }

    @Bean @Profile("dev & !prod")
    public KeyPair developmentJwtKeys() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean @Profile("!dev | prod")
    public KeyPair configuredJwtKeys(
            @Value("${app.jwt.public-key}") Resource publicKey,
            @Value("${app.jwt.private-key}") Resource privateKey) throws Exception {
        try (var pub = publicKey.getInputStream(); var priv = privateKey.getInputStream()) {
            RSAPublicKey publicRsa = RsaKeyConverters.x509().convert(pub);
            RSAPrivateKey privateRsa = RsaKeyConverters.pkcs8().convert(priv);
            if (publicRsa == null || privateRsa == null || publicRsa.getModulus().bitLength() < 2048
                    || !publicRsa.getModulus().equals(privateRsa.getModulus())) {
                throw new IllegalArgumentException("Configure um par RSA válido de pelo menos 2048 bits.");
            }
            return new KeyPair(publicRsa, privateRsa);
        }
    }

    @Bean public JwtEncoder jwtEncoder(KeyPair keys) {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) keys.getPublic())
                .privateKey((RSAPrivateKey) keys.getPrivate()).keyID("fisiotech-rsa").build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    @Bean public JwtDecoder jwtDecoder(KeyPair keys, Clock clock,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keys.getPublic())
                .signatureAlgorithm(SignatureAlgorithm.RS256).build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator(java.time.Duration.ZERO);
        timestamps.setClock(clock);
        OAuth2TokenValidator<Jwt> claims = jwt -> {
            boolean valid = jwt.getExpiresAt() != null && jwt.getIssuedAt() != null
                    && !jwt.getIssuedAt().isAfter(clock.instant())
                    && jwt.getExpiresAt().isAfter(jwt.getIssuedAt())
                    && jwt.getAudience() != null && jwt.getAudience().contains(audience)
                    && jwt.getSubject() != null && jwt.getId() != null
                    && jwt.getClaimAsString("sid") != null;
            return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Token inválido.", null));
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamps, new JwtIssuerValidator(issuer), claims));
        return decoder;
    }
}
