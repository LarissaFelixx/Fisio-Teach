package com.app.fisiotech.consulta.service;
import com.app.fisiotech.consulta.entity.*; import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.paciente.entity.Paciente; import com.app.fisiotech.profissional.entity.Profissional;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import org.mockito.*; import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime; import java.util.List; import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {
 @Mock ConsultaRepository consultas; @InjectMocks AgendaService service;
 @Test void consolidaIndicadoresDoPeriodo() throws Exception {
  var p=new Profissional("P","p@x","h","R","E"); var a=new Paciente("A","a@x","h",p); var b=new Paciente("B","b@x","h",p); setId(a,1L);setId(b,2L);
  var realizada=new Consulta(a,p,LocalDateTime.now(),TipoConsulta.PRESENCIAL,null,null);realizada.setStatus(StatusConsulta.REALIZADA);
  var cancelada=new Consulta(b,p,LocalDateTime.now(),TipoConsulta.ONLINE,null,null);cancelada.setStatus(StatusConsulta.CANCELADA);
  when(consultas.buscarAgenda(eq(1L),any(),any(),isNull(),isNull(),isNull())).thenReturn(List.of(realizada,cancelada));
  var r=service.indicadores(1L,LocalDateTime.now().minusDays(1),LocalDateTime.now().plusDays(1));
  assertThat(r.totalConsultas()).isEqualTo(2);assertThat(r.pacientesAtendidos()).isEqualTo(1);assertThat(r.taxaCancelamento()).isEqualTo(50.0);
 }
 private void setId(Object o,Long id)throws Exception{var f=o.getClass().getDeclaredField("id");f.setAccessible(true);f.set(o,id);}
}
