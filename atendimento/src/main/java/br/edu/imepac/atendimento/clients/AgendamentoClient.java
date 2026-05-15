package br.edu.imepac.atendimento.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AgendamentoClient {

    private final RestTemplate restTemplate;
    private final String agendamentoUrl;

    public AgendamentoClient(RestTemplate restTemplate,
                             @Value("${agendamento.url}") String agendamentoUrl) {
        this.restTemplate = restTemplate;
        this.agendamentoUrl = agendamentoUrl;
    }

    // notifica o agendamento que a consulta foi realizada
    public void confirmarRealizacao(Long consultaId) {
        String url = agendamentoUrl + "/v1/consultas/" + consultaId + "/realizar";
        restTemplate.patchForObject(url, null, Void.class);
    }
}
