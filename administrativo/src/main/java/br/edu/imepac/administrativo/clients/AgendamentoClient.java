package br.edu.imepac.administrativo.clients;

import br.edu.imepac.administrativo.clients.dto.ContagemConsultasDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

// cliente que o relatorio do administrativo usa pra contar consultas do dia no agendamento
@Component
public class AgendamentoClient {

    private final RestTemplate restTemplate;
    private final String agendamentoUrl;

    public AgendamentoClient(RestTemplate restTemplate,
                             @Value("${agendamento.url}") String agendamentoUrl) {
        this.restTemplate = restTemplate;
        this.agendamentoUrl = agendamentoUrl;
    }

    // chama o endpoint dedicado /v1/consultas/contagem — payload e so {total}, nao baixa lista
    // fail-safe: se o agendamento estiver indisponivel ou retornar erro, devolve 0 e o relatorio
    // mostra zero em vez de quebrar a chamada inteira do administrativo
    public long contarConsultasPorData(LocalDate data) {
        try {
            ContagemConsultasDTO resp = restTemplate.getForObject(
                    agendamentoUrl + "/v1/consultas/contagem?data=" + data,
                    ContagemConsultasDTO.class);
            return resp != null ? resp.getTotal() : 0L;
        } catch (RestClientException e) {
            return 0L;
        }
    }
}
