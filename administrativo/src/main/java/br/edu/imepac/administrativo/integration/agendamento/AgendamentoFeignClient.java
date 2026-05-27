package br.edu.imepac.administrativo.integration.agendamento;

import br.edu.imepac.administrativo.integration.agendamento.dto.ContagemConsultasDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "agendamento", url = "${agendamento.url}", configuration = FeignConfig.class)
public interface AgendamentoFeignClient {

    @GetMapping("/v1/consultas/contagem")
    ContagemConsultasDTO contarConsultasPorData(@RequestParam("data") String data);
}
