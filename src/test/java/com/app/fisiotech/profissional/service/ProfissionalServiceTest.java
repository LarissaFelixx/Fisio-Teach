package com.app.fisiotech.profissional.service;

import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.exception.EmailJaCadastradoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.exception.SenhaAtualInvalidaException;
import com.app.fisiotech.profissional.dto.ProfissionalCreateRequest;
import com.app.fisiotech.profissional.dto.ProfissionalUpdateRequest;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfissionalService profissionalService;

    private ProfissionalCreateRequest requestValido() {
        return new ProfissionalCreateRequest(
                "Ana Souza", "ana@fisiotech.com", "senha123", "CREFITO-1", "Ortopedia",
                new BigDecimal("150.00"), List.of("Unimed"), null, null, null, null
        );
    }

    @Test
    void naoDevePermitirCadastrarComEmailJaExistente() {
        when(profissionalRepository.existsByEmail("ana@fisiotech.com")).thenReturn(true);

        assertThatThrownBy(() -> profissionalService.criar(requestValido()))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(profissionalRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirCadastrarComRegistroProfissionalJaExistente() {
        when(profissionalRepository.existsByEmail(any())).thenReturn(false);
        when(profissionalRepository.existsByRegistroProfissional("CREFITO-1")).thenReturn(true);

        assertThatThrownBy(() -> profissionalService.criar(requestValido()))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(profissionalRepository, never()).save(any());
    }

    @Test
    void devePermitirCadastrarProfissionalValido() {
        when(profissionalRepository.existsByEmail(any())).thenReturn(false);
        when(profissionalRepository.existsByRegistroProfissional(any())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        Profissional resultado = profissionalService.criar(requestValido());

        assertThat(resultado.getEmail()).isEqualTo("ana@fisiotech.com");
        assertThat(resultado.getSenha()).isEqualTo("hash");
        assertThat(resultado.getConveniosAceitos()).containsExactly("Unimed");
    }

    @Test
    void deveUsarListaVaziaQuandoConveniosNaoInformados() {
        when(profissionalRepository.existsByEmail(any())).thenReturn(false);
        when(profissionalRepository.existsByRegistroProfissional(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfissionalCreateRequest request = new ProfissionalCreateRequest(
                "Carlos Reis", "carlos@fisiotech.com", "senha123", "CREFITO-2", "Neurologia",
                new BigDecimal("180.00"), null, null, null, null, null
        );

        Profissional resultado = profissionalService.criar(request);

        assertThat(resultado.getConveniosAceitos()).isEmpty();
    }

    @Test
    void buscaPublicaDeveFiltrarPorEspecialidadeIgnorandoCaixa() {
        Profissional ana = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        Profissional carlos = new Profissional("Carlos Reis", "carlos@fisiotech.com", "hash", "CREFITO-2", "Neurologia");
        when(profissionalRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(ana, carlos));

        List<Profissional> resultado = profissionalService.buscarPublico(null, "ortopedia");

        assertThat(resultado).containsExactly(ana);
    }

    @Test
    void deveLancarNaoEncontradoQuandoProfissionalNaoExiste() {
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // --- atualizar: senha e opcional (nao deve forcar reset a cada edicao) ---

    private Profissional profissionalExistente() {
        Profissional profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "senhaAntigaHash", "CREFITO-1", "Ortopedia");
        try {
            var field = Profissional.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(profissional, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return profissional;
    }

    private ProfissionalUpdateRequest updateRequest(String senha) {
        return new ProfissionalUpdateRequest(
                "Ana Souza", "ana@fisiotech.com", senha, "CREFITO-1", "Ortopedia",
                new BigDecimal("150.00"), List.of("Unimed"), null, null, null, null
        );
    }

    @Test
    void naoDeveAlterarSenhaQuandoCampoNaoEEnviadoNaEdicao() {
        Profissional existente = profissionalExistente();
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(profissionalRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(profissionalRepository.existsByRegistroProfissionalAndIdNot(any(), any())).thenReturn(false);
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        Profissional resultado = profissionalService.atualizar(1L, updateRequest(null));

        assertThat(resultado.getSenha()).isEqualTo("senhaAntigaHash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void naoDeveAlterarSenhaQuandoCampoEnviadoEmBranco() {
        Profissional existente = profissionalExistente();
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(profissionalRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(profissionalRepository.existsByRegistroProfissionalAndIdNot(any(), any())).thenReturn(false);
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        Profissional resultado = profissionalService.atualizar(1L, updateRequest("  "));

        assertThat(resultado.getSenha()).isEqualTo("senhaAntigaHash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveAlterarSenhaQuandoNovaSenhaEEnviada() {
        Profissional existente = profissionalExistente();
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(profissionalRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(profissionalRepository.existsByRegistroProfissionalAndIdNot(any(), any())).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(inv -> inv.getArgument(0));

        Profissional resultado = profissionalService.atualizar(1L, updateRequest("novaSenha123"));

        assertThat(resultado.getSenha()).isEqualTo("novoHash");
    }

    // --- alterarSenha (autoatendimento, F-02) ---

    @Test
    void naoDevePermitirAlterarPropriaSenhaComSenhaAtualIncorreta() {
        Profissional existente = profissionalExistente();
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("senhaErrada", "senhaAntigaHash")).thenReturn(false);

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaErrada", "novaSenha123");

        assertThatThrownBy(() -> profissionalService.alterarSenha(1L, request))
                .isInstanceOf(SenhaAtualInvalidaException.class);

        verify(profissionalRepository, never()).save(any());
    }

    @Test
    void devePermitirAlterarPropriaSenhaComSenhaAtualCorreta() {
        Profissional existente = profissionalExistente();
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("senhaAntiga123", "senhaAntigaHash")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaAntiga123", "novaSenha123");

        profissionalService.alterarSenha(1L, request);

        assertThat(existente.getSenha()).isEqualTo("novoHash");
        verify(profissionalRepository).save(existente);
    }
}
