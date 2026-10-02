package com.app.fisiotech.prontuario.service;

import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import com.app.fisiotech.prontuario.dto.PlanoRequest;
import com.app.fisiotech.prontuario.entity.*;
import com.app.fisiotech.prontuario.repository.*;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import org.junit.jupiter.api.*; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate; import java.util.Optional;
import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {
 @Mock PacienteRepository pacientes; @Mock ProfissionalRepository profissionais; @Mock ConsultaRepository consultas;
 @Mock EvolucaoClinicaRepository evolucoes; @Mock PlanoTerapeuticoRepository planos; @InjectMocks ProntuarioService service;
 @Test void novaRevisaoPreservaAnteriorComoSubstituida() throws Exception {
  var prof=new Profissional("P","p@x.com","h","R","E"); var paciente=new Paciente("X","x@x.com","h",prof);
  setId(prof,1L); setId(paciente,2L); var anterior=new PlanoTerapeutico(paciente,prof,1,"o","c",LocalDate.now(),null,false);
  when(pacientes.findById(2L)).thenReturn(Optional.of(paciente)); when(profissionais.getReferenceById(1L)).thenReturn(prof);
  when(planos.findFirstByPacienteIdAndProfissionalIdOrderByRevisaoDesc(2L,1L)).thenReturn(Optional.of(anterior)); when(planos.save(any())).thenAnswer(i->i.getArgument(0));
  var novo=service.revisarPlano(2L,1L,new PlanoRequest("novo","conduta",LocalDate.now(),null,true));
  assertThat(anterior.getStatus()).isEqualTo(StatusPlano.SUBSTITUIDO); assertThat(novo.getRevisao()).isEqualTo(2); assertThat(novo.isVisivelPaciente()).isTrue();
 }
 private void setId(Object value,Long id)throws Exception{var f=value.getClass().getDeclaredField("id");f.setAccessible(true);f.set(value,id);}
}
