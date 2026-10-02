package com.app.fisiotech.common.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PageRequestFactoryTest {
    @Test
    void deveLimitarTamanhoECamposDeOrdenacao() {
        var pageable = PageRequestFactory.create(-2, 500, "senha", "desc", Set.of("id", "nome"));

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("id").getDirection().isDescending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("senha")).isNull();
    }
}
