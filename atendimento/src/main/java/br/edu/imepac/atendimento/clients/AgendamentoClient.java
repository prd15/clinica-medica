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
    // 404 -> Optional.empty() (consulta nao existe); qualquer outra falha de comunicacao
    // (timeout, 5xx, conexao recusada) -> ServicoIndisponivelException -> handler vira 503
    public Optional<ConsultaRefDTO> buscarConsulta(Long consultaId) {
        try {
            ConsultaRefDTO consulta = restTemplate.getForObject(
                    agendamentoUrl + "/v1/consultas/" + consultaId, ConsultaRefDTO.class);
            return Optional.ofNullable(consulta);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException(
                    "Agendamento indisponivel ao buscar consulta " + consultaId, e);
        }
    }

    // notifica o agendamento que a consulta foi realizada
    public void confirmarRealizacao(Long consultaId) {
        String url = agendamentoUrl + "/v1/consultas/" + consultaId + "/realizar";
        restTemplate.patchForObject(url, null, Void.class);
    }
}
