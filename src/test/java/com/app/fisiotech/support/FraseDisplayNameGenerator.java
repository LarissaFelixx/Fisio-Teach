package com.app.fisiotech.support;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Transforma nomes em camelCase em frases legíveis nos relatórios de teste:
 * {@code deveRecusarCodigoExpirado} vira "Deve recusar codigo expirado" e
 * {@code RecuperacaoSenhaServiceTest} vira "Recuperacao senha service".
 * Registrado como padrão em src/test/resources/junit-platform.properties;
 * um {@code @DisplayName} explícito continua tendo prioridade.
 */
public class FraseDisplayNameGenerator extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        return frase(testClass.getSimpleName().replaceFirst("Tests?$", ""));
    }

    @Override
    public String generateDisplayNameForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> nestedClass) {
        return frase(nestedClass.getSimpleName());
    }

    @Override
    public String generateDisplayNameForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass, Method testMethod) {
        return frase(testMethod.getName());
    }

    static String frase(String identificador) {
        // Quebra antes de cada maiúscula que inicia palavra e entre letras e dígitos,
        // mantendo siglas juntas (ex: "JwtConfig" -> "Jwt config", "De5Tentativas" -> "de 5 tentativas").
        String separado = identificador
                .replaceAll("([a-z])([A-Z0-9])", "$1 $2")
                .replaceAll("([0-9])([A-Za-z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                .replace('_', ' ')
                .trim();
        if (separado.isEmpty()) {
            return identificador;
        }

        StringBuilder resultado = new StringBuilder();
        for (String palavra : separado.split("\\s+")) {
            if (!resultado.isEmpty()) {
                resultado.append(' ');
            }
            // Siglas (ex: "JWT", "HTTP") ficam como estão; o resto vai para minúsculo.
            boolean sigla = palavra.length() > 1 && palavra.equals(palavra.toUpperCase());
            resultado.append(sigla ? palavra : palavra.toLowerCase());
        }
        return Character.toUpperCase(resultado.charAt(0)) + resultado.substring(1);
    }
}
