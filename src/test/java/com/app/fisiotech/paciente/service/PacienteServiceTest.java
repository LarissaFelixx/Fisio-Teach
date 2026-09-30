package com.app.fisiotech.paciente.service;

import com.app.fisiotech.exception.EmailJaCadastradoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.exception.SenhaAtualInvalidaException;
import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.paciente.dto.PacienteCreateRequest;
import com.app.fisiotech.paciente.dto.PacienteUpdateRequest;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PacienteService pacienteService;

    private Profissional profissional;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        setId(profissional, 1L);

        paciente = new Paciente("Joao Silva", "joao@paciente.com", "senhaCriptografada", profissional);
        setId(paciente, 1L);
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- cadastro (público e pelo profissional) ---

    @Test
    void naoDevePermitirAutocadastroComEmailJaExistente() {
        when(pacienteRepository.existsByEmail("joao@paciente.com")).thenReturn(true);

        PacienteCreateRequest request = new PacienteCreateRequest("Joao Silva", "joao@paciente.com", "senha123");

        assertThatThrownBy(() -> pacienteService.cadastrarPublico(request))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void autocadastroDeveNascerSemProfissionalVinculado() {
        when(pacienteRepository.existsByEmail("joao@paciente.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteCreateRequest request = new PacienteCreateRequest("Joao Silva", "joao@paciente.com", "senha123");

        Paciente resultado = pacienteService.cadastrarPublico(request);

        assertThat(resultado.getProfissional()).isNull();
    }

    @Test
    void deveNormalizarEmailParaMinusculoAoCadastrar() {
        when(pacienteRepository.existsByEmail("joao@paciente.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteCreateRequest request = new PacienteCreateRequest("Joao Silva", "  JOAO@Paciente.COM  ", "senha123");

        Paciente resultado = pacienteService.cadastrarPublico(request);

        assertThat(resultado.getEmail()).isEqualTo("joao@paciente.com");
    }

    @Test
    void pacienteCadastradoPeloProfissionalDeveNascerVinculadoAEle() {
        when(pacienteRepository.existsByEmail("maria@paciente.com")).thenReturn(false);
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        PacienteCreateRequest request = new PacienteCreateRequest("Maria Lima", "maria@paciente.com", "senha123");

        Paciente resultado = pacienteService.criar(request, 1L);

        assertThat(resultado.getProfissional()).isEqualTo(profissional);
    }

    // --- isolamento por dono ---

    @Test
    void naoDevePermitirProfissionalVerPacienteDeOutroProfissional() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> pacienteService.buscarPorId(1L, 2L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Paciente não encontrado.");
    }

    @Test
    void naoDevePermitirVerPacienteSemProfissionalVinculadoComoSeFosseSeu() {
        paciente.setProfissional(null);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        assertThatThrownBy(() -> pacienteService.buscarPorId(1L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // --- troca de senha ---

    @Test
    void naoDevePermitirTrocarSenhaComSenhaAtualIncorreta() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(passwordEncoder.matches("senhaErrada", "senhaCriptografada")).thenReturn(false);

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaErrada", "novaSenha123");

        assertThatThrownBy(() -> pacienteService.alterarSenha(1L, request))
                .isInstanceOf(SenhaAtualInvalidaException.class);

        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void devePermitirTrocarSenhaComSenhaAtualCorreta() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(passwordEncoder.matches("senhaAtual123", "senhaCriptografada")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaAtual123", "novaSenha123");

        pacienteService.alterarSenha(1L, request);

        assertThat(paciente.getSenha()).isEqualTo("novoHash");
        verify(pacienteRepository).save(paciente);
    }

    // --- atualizar (profissional editando o proprio paciente): senha e opcional ---

    private PacienteUpdateRequest updateRequest(String senha) {
        return new PacienteUpdateRequest("Joao Silva", "joao@paciente.com", senha, null, null, null, null, null, null, null);
    }

    @Test
    void naoDeveAlterarSenhaDoPacienteQuandoCampoNaoEEnviadoNaEdicao() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Paciente resultado = pacienteService.atualizar(1L, updateRequest(null), 1L);

        assertThat(resultado.getSenha()).isEqualTo("senhaCriptografada");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void naoDeveAlterarSenhaDoPacienteQuandoCampoEnviadoEmBranco() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Paciente resultado = pacienteService.atualizar(1L, updateRequest("   "), 1L);

        assertThat(resultado.getSenha()).isEqualTo("senhaCriptografada");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveAlterarSenhaDoPacienteQuandoNovaSenhaEEnviada() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsByEmailAndIdNot(any(), any())).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Paciente resultado = pacienteService.atualizar(1L, updateRequest("novaSenha123"), 1L);

        assertThat(resultado.getSenha()).isEqualTo("novoHash");
    }
}
