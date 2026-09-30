package com.app.fisiotech.mensagem.service;

import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.mensagem.dto.MensagemCreateRequest;
import com.app.fisiotech.mensagem.entity.AutorMensagem;
import com.app.fisiotech.mensagem.entity.Mensagem;
import com.app.fisiotech.mensagem.repository.MensagemRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

    @Mock
    private MensagemRepository mensagemRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private MensagemService mensagemService;

    private Profissional profissional;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        setId(profissional, 1L);

        paciente = new Paciente("Joao Silva", "joao@paciente.com", "hash", profissional);
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

    // --- Regra central: mensagem só é permitida se existe consulta entre as partes ---

    @Test
    void naoDevePermitirEnviarMensagemSemConsultaEntreAsPartes() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByPacienteIdAndProfissionalId(1L, 1L)).thenReturn(false);

        MensagemCreateRequest request = new MensagemCreateRequest(1L, AutorMensagem.PROFISSIONAL, "Oi Joao");

        assertThatThrownBy(() -> mensagemService.enviar(request, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Paciente não encontrado.");

        verify(mensagemRepository, never()).save(any());
    }

    @Test
    void devePermitirEnviarMensagemQuandoExisteConsultaEntreAsPartes() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByPacienteIdAndProfissionalId(1L, 1L)).thenReturn(true);
        when(mensagemRepository.save(any(Mensagem.class))).thenAnswer(inv -> inv.getArgument(0));

        MensagemCreateRequest request = new MensagemCreateRequest(1L, AutorMensagem.PROFISSIONAL, "Oi Joao");

        Mensagem resultado = mensagemService.enviar(request, 1L);

        assertThat(resultado.getConteudo()).isEqualTo("Oi Joao");
        assertThat(resultado.getAutor()).isEqualTo(AutorMensagem.PROFISSIONAL);
        verify(mensagemRepository).save(any(Mensagem.class));
    }

    @Test
    void naoDevePermitirPacienteResponderSemConsultaEntreAsPartes() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByPacienteIdAndProfissionalId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> mensagemService.enviarComoPaciente(1L, 1L, "Oi Ana"))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(mensagemRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirListarConversaSemConsultaEntreAsPartes() {
        when(consultaRepository.existsByPacienteIdAndProfissionalId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> mensagemService.listarPorPaciente(1L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void devePermitirListarConversaQuandoExisteConsultaMesmoQueCancelada() {
        // A posse da conversa é "existe consulta" (qualquer status, inclusive cancelada),
        // não "esse paciente é meu" -- a regra documentada no README.
        when(consultaRepository.existsByPacienteIdAndProfissionalId(1L, 1L)).thenReturn(true);
        when(mensagemRepository.findByPacienteIdAndProfissionalId(eq(1L), eq(1L), any()))
                .thenReturn(java.util.List.of());

        mensagemService.listarPorPaciente(1L, 1L);

        verify(mensagemRepository).findByPacienteIdAndProfissionalId(eq(1L), eq(1L), any());
    }

    @Test
    void deveLancarNaoEncontradoQuandoPacienteNaoExiste() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        MensagemCreateRequest request = new MensagemCreateRequest(99L, AutorMensagem.PROFISSIONAL, "Oi");

        assertThatThrownBy(() -> mensagemService.enviar(request, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Paciente não encontrado.");
    }
}
