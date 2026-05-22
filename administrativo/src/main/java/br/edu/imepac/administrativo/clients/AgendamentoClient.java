package br.edu.imepac.administrativo.clients;

import br.edu.imepac.administrativo.clients.dto.ConsultaDoDiaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

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

    // fail-safe: se o agendamento estiver indisponivel ou retornar erro, devolve lista vazia
    // o relatorio mostra 0 consultas em vez de quebrar a chamada inteira do administrativo
    public List<ConsultaDoDiaDTO> listarConsultasPorData(LocalDate data) {
        try {
            ResponseEntity<List<ConsultaDoDiaDTO>> response = restTemplate.exchange(
                    agendamentoUrl + "/v1/consultas?data=" + data,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<ConsultaDoDiaDTO>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            return Collections.emptyList();
        }
    }
}
