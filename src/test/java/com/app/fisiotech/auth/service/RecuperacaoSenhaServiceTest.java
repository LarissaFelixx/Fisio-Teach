package com.app.fisiotech.auth.service;

import com.app.fisiotech.admin.entity.Admin;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.auth.dto.RecuperarSenhaRequest;
import com.app.fisiotech.auth.dto.RedefinirSenhaRequest;
import com.app.fisiotech.auth.entity.CodigoRecuperacaoSenha;
import com.app.fisiotech.auth.repository.CodigoRecuperacaoSenhaRepository;
import com.app.fisiotech.exception.CodigoRecuperacaoInvalidoException;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 30, 10, 0);
    private static final String EMAIL = "joao@paciente.com";

    @Mock
    private CodigoRecuperacaoSenhaRepository codigoRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EnvioCodigoRecuperacaoService envioCodigo;

    private RecuperacaoSenhaService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA.atZone(ZONA).toInstant(), ZONA);
        service = new RecuperacaoSenhaService(codigoRepository, profissionalRepository, adminRepository,
                pacienteRepository, passwordEncoder, envioCodigo, clock, 15);
    }

    private CodigoRecuperacaoSenha codigoCriadoHa(long minutos) {
        LocalDateTime criadoEm = AGORA.minusMinutes(minutos);
        return new CodigoRecuperacaoSenha(EMAIL, "codigoHash", criadoEm, criadoEm.plusMinutes(15));
    }

    private RedefinirSenhaRequest redefinir(String codigo) {
        return new RedefinirSenhaRequest(EMAIL, codigo, "novaSenha123");
    }

    // ---------- solicitarCodigo ----------

    @Test
    void naoDeveGerarNemEnviarCodigoQuandoEmailNaoExiste() {
        service.solicitarCodigo(new RecuperarSenhaRequest("ninguem@nada.com"));

        verify(codigoRepository, never()).save(any());
        verifyNoInteractions(envioCodigo);
    }

    @Test
    void deveGerarCodigoDe6DigitosSalvarSoOHashEEnviarPorEmail() {
        when(pacienteRepository.existsByEmail(EMAIL)).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("codigoHash");

        service.solicitarCodigo(new RecuperarSenhaRequest("  JOAO@Paciente.com "));

        ArgumentCaptor<String> codigoEnviado = ArgumentCaptor.forClass(String.class);
        verify(envioCodigo).enviar(eq(EMAIL), codigoEnviado.capture());
        assertThat(codigoEnviado.getValue()).matches("\\d{6}");
        verify(passwordEncoder).encode(codigoEnviado.getValue());

        ArgumentCaptor<CodigoRecuperacaoSenha> salvo = ArgumentCaptor.forClass(CodigoRecuperacaoSenha.class);
        verify(codigoRepository).save(salvo.capture());
        assertThat(salvo.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(salvo.getValue().getCodigoHash()).isEqualTo("codigoHash");
        assertThat(salvo.getValue().getExpiraEm()).isEqualTo(AGORA.plusMinutes(15));
    }

    @Test
    void deveInvalidarCodigosAnterioresAoGerarUmNovo() {
        when(profissionalRepository.existsByEmail(EMAIL)).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("codigoHash");

        service.solicitarCodigo(new RecuperarSenhaRequest(EMAIL));

        var ordem = inOrder(codigoRepository);
        ordem.verify(codigoRepository).deleteByEmail(EMAIL);
        ordem.verify(codigoRepository).save(any());
    }

    // ---------- redefinirSenha ----------

    @Test
    void deveRedefinirSenhaDoPacienteEMarcarCodigoComoUsado() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(5);
        Paciente paciente = new Paciente("Joao", EMAIL, "senhaAntigaHash", null);
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        when(passwordEncoder.matches("123456", "codigoHash")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaHash");
        when(pacienteRepository.findByEmail(EMAIL)).thenReturn(Optional.of(paciente));

        service.redefinirSenha(redefinir("123456"));

        assertThat(paciente.getSenha()).isEqualTo("novaSenhaHash");
        assertThat(codigo.isUsado()).isTrue();
    }

    @Test
    void deveSeguirMesmaPrecedenciaDoLoginAoAtualizarSenha() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(5);
        Profissional profissional = new Profissional("Ana", EMAIL, "hashProf", "CREFITO-1", "Ortopedia");
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        when(passwordEncoder.matches("123456", "codigoHash")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaHash");
        when(profissionalRepository.findByEmail(EMAIL)).thenReturn(Optional.of(profissional));

        service.redefinirSenha(redefinir("123456"));

        assertThat(profissional.getSenha()).isEqualTo("novaSenhaHash");
        verify(adminRepository, never()).findByEmail(any());
        verify(pacienteRepository, never()).findByEmail(any());
    }

    @Test
    void deveRedefinirSenhaDoAdmin() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(5);
        Admin admin = new Admin("Admin", EMAIL, "hashAdmin");
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        when(passwordEncoder.matches("123456", "codigoHash")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaHash");
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));

        service.redefinirSenha(redefinir("123456"));

        assertThat(admin.getSenha()).isEqualTo("novaSenhaHash");
    }

    @Test
    void deveFalharQuandoNaoHaCodigoParaOEmail() {
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.redefinirSenha(redefinir("123456")))
                .isInstanceOf(CodigoRecuperacaoInvalidoException.class);
    }

    @Test
    void deveContarTentativaQuandoCodigoEstaErrado() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(5);
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        when(passwordEncoder.matches("000000", "codigoHash")).thenReturn(false);

        assertThatThrownBy(() -> service.redefinirSenha(redefinir("000000")))
                .isInstanceOf(CodigoRecuperacaoInvalidoException.class);

        assertThat(codigo.getTentativas()).isEqualTo(1);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveRecusarCodigoCorretoDepoisDe5TentativasErradas() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(5);
        for (int i = 0; i < RecuperacaoSenhaService.MAX_TENTATIVAS; i++) {
            codigo.registrarTentativaInvalida();
        }
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        lenient().when(passwordEncoder.matches("123456", "codigoHash")).thenReturn(true);

        assertThatThrownBy(() -> service.redefinirSenha(redefinir("123456")))
                .isInstanceOf(CodigoRecuperacaoInvalidoException.class);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveRecusarCodigoExpirado() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(16);
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        lenient().when(passwordEncoder.matches("123456", "codigoHash")).thenReturn(true);

        assertThatThrownBy(() -> service.redefinirSenha(redefinir("123456")))
                .isInstanceOf(CodigoRecuperacaoInvalidoException.class);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deveRecusarCodigoJaUsado() {
        CodigoRecuperacaoSenha codigo = codigoCriadoHa(5);
        codigo.marcarComoUsado();
        when(codigoRepository.findFirstByEmailOrderByIdDesc(EMAIL)).thenReturn(Optional.of(codigo));
        lenient().when(passwordEncoder.matches("123456", "codigoHash")).thenReturn(true);

        assertThatThrownBy(() -> service.redefinirSenha(redefinir("123456")))
                .isInstanceOf(CodigoRecuperacaoInvalidoException.class);

        verify(passwordEncoder, never()).encode(any());
    }

}
