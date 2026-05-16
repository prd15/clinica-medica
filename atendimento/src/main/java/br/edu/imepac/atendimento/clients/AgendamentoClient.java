package br.edu.imepac.atendimento.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgendamentoClient {

    private final RestTemplate restTemplate;
    private final String agendamentoUrl;

    public AgendamentoClient(RestTemplate restTemplate,
                             @Value("${agendamento.url:http://localhost:8082}") String agendamentoUrl) {
        this.restTemplate = restTemplate;
        this.agendamentoUrl = agendamentoUrl;
    }

    public void confirmarRealizacao(Long consultaId) {
        restTemplate.put(agendamentoUrl + "/v1/consultas/" + consultaId + "/realizar", null);
    }
}
