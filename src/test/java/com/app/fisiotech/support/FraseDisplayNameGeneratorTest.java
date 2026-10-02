package com.app.fisiotech.support;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FraseDisplayNameGeneratorTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "deveRecusarCodigoExpirado, Deve recusar codigo expirado",
            "deveRecusarCodigoCorretoDepoisDe5TentativasErradas, Deve recusar codigo correto depois de 5 tentativas erradas",
            "RecuperacaoSenhaService, Recuperacao senha service",
            "JwtConfig, Jwt config",
            "loginComJWTValido, Login com JWT valido",
            "contextLoads, Context loads"
    })
    void deveTransformarIdentificadorEmFrase(String identificador, String esperado) {
        assertThat(FraseDisplayNameGenerator.frase(identificador)).isEqualTo(esperado);
    }
}
