package br.edu.imepac.agendamento.integration.administrativo;

import br.edu.imepac.agendamento.integration.administrativo.dto.ConvenioRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.MedicoRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.PacienteRefDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "administrativo", url = "${administrativo.url}", configuration = FeignConfig.class)
public interface AdministrativoFeignClient {

    @GetMapping("/v1/convenios/{id}")
    ConvenioRefDTO buscarConvenio(@PathVariable("id") Long id);

    @GetMapping("/v1/medicos/{id}")
    MedicoRefDTO buscarMedico(@PathVariable("id") Long id);

    @GetMapping("/v1/pacientes/{id}")
    PacienteRefDTO buscarPaciente(@PathVariable("id") Long id);
}
