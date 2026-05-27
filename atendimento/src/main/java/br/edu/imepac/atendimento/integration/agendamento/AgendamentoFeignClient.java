package br.edu.imepac.atendimento.integration.agendamento;

import br.edu.imepac.atendimento.integration.agendamento.dto.ConsultaRefDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "agendamento", url = "${agendamento.url}", configuration = FeignConfig.class)
public interface AgendamentoFeignClient {

    @GetMapping("/v1/consultas/{id}")
    ConsultaRefDTO buscarConsulta(@PathVariable Long id);

    @PatchMapping("/v1/consultas/{id}/realizar")
    void confirmarRealizacao(@PathVariable Long id);
}
