package br.edu.imepac.agendamento.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AdministrativoClient {

    private final RestTemplate restTemplate;

    // nome bate exatamente com administrativo.url do application.properties do agendamento
    @Value("${administrativo.url}")
    private String adminUrl;

    public AdministrativoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // GET /v1/convenios/{id} no administrativo — devolve o objeto bruto em Map
    @SuppressWarnings("rawtypes")
    public ResponseEntity<Map> buscarConvenio(Long id) {
        return restTemplate.getForEntity(adminUrl + "/v1/convenios/" + id, Map.class);
    }

    // valida se o convenio existe E esta ativo — chamada antes de agendar consulta
    // 404 do administrativo significa "convenio nao existe" — tratamos como inativo
    // 5xx/timeout/connection refused: fail-safe, tambem retorna false (nao agenda em duvida)
    public boolean isConvenioAtivo(Long convenioId) {
        try {
            ResponseEntity<Map> response = buscarConvenio(convenioId);
            if (response.getBody() == null) {
                return false;
            }
            Object ativo = response.getBody().get("ativo");
            return Boolean.TRUE.equals(ativo);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            // qualquer falha de comunicacao (5xx, timeout, conexao recusada): nao agenda
            // melhor falso negativo aqui do que agendar uma consulta com convenio invalido
            return false;
        }
    }

    // GET /v1/medicos/{id} — pode ser usado pra validar existencia do medico antes de agendar
    @SuppressWarnings("rawtypes")
    public ResponseEntity<Map> buscarMedico(Long id) {
        try {
            return restTemplate.getForEntity(adminUrl + "/v1/medicos/" + id, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /v1/pacientes/{id}
    @SuppressWarnings("rawtypes")
    public ResponseEntity<Map> buscarPaciente(Long id) {
        try {
            return restTemplate.getForEntity(adminUrl + "/v1/pacientes/" + id, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }
}
