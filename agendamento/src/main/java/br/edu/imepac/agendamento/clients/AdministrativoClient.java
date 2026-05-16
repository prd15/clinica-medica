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
    private final String adminUrl;

    public AdministrativoClient(RestTemplate restTemplate,
                                @Value("${administrativo.url:http://localhost:8081}") String adminUrl) {
        this.restTemplate = restTemplate;
        this.adminUrl = adminUrl;
    }

    public boolean isConvenioAtivo(Long convenioId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(adminUrl + "/v1/convenios/" + convenioId, Map.class);
            if (response.getBody() == null) {
                return false;
            }
            Object ativo = response.getBody().get("ativo");
            return Boolean.TRUE.equals(ativo);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }

    // valida se o medico existe E esta ativo — mesma logica do convenio
    public boolean isMedicoAtivo(Long medicoId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(adminUrl + "/v1/medicos/" + medicoId, Map.class);
            if (response.getBody() == null) {
                return false;
            }
            Object ativo = response.getBody().get("ativo");
            return Boolean.TRUE.equals(ativo);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }

    // paciente nao tem campo ativo — valida somente existencia antes de agendar
    public boolean isPacienteExistente(Long pacienteId) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(adminUrl + "/v1/pacientes/" + pacienteId, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }
}
