package br.edu.imepac.atendimento.clients;

import br.edu.imepac.atendimento.clients.dto.ConsultaRefDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class AgendamentoClient {

    private final RestTemplate restTemplate;
    private final String agendamentoUrl;

    public AgendamentoClient(RestTemplate restTemplate,
                             @Value("${agendamento.url}") String agendamentoUrl) {
        this.restTemplate = restTemplate;
        this.agendamentoUrl = agendamentoUrl;
    }

    // busca a consulta no agendamento antes de registrar o atendimento
    // fail-safe: 404 ou falha de comunicacao retornam vazio — quem chama trata como "nao validavel"
    public Optional<ConsultaRefDTO> buscarConsulta(Long consultaId) {
        try {
            ConsultaRefDTO consulta = restTemplate.getForObject(
                    agendamentoUrl + "/v1/consultas/" + consultaId, ConsultaRefDTO.class);
            return Optional.ofNullable(consulta);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    // notifica o agendamento que a consulta foi realizada
    public void confirmarRealizacao(Long consultaId) {
        String url = agendamentoUrl + "/v1/consultas/" + consultaId + "/realizar";
        restTemplate.patchForObject(url, null, Void.class);
    }
}
