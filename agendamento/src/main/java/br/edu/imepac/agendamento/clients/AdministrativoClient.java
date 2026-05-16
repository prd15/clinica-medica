package br.edu.imepac.agendamento.clients;

import br.edu.imepac.agendamento.clients.dto.ConvenioRefDTO;
import br.edu.imepac.agendamento.clients.dto.MedicoRefDTO;
import br.edu.imepac.agendamento.clients.dto.PacienteRefDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AdministrativoClient {

    private final RestTemplate restTemplate;

    @Value("${administrativo.url}")
    private String adminUrl;

    public AdministrativoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<ConvenioRefDTO> buscarConvenio(Long id) {
        return restTemplate.getForEntity(adminUrl + "/v1/convenios/" + id, ConvenioRefDTO.class);
    }

    public boolean isConvenioAtivo(Long convenioId) {
        try {
            ResponseEntity<ConvenioRefDTO> response = buscarConvenio(convenioId);
            ConvenioRefDTO body = response.getBody();
            if (body == null) {
                return false;
            }
            return Boolean.TRUE.equals(body.getAtivo());
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }

    public ResponseEntity<MedicoRefDTO> buscarMedico(Long id) {
        try {
            return restTemplate.getForEntity(adminUrl + "/v1/medicos/" + id, MedicoRefDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean isMedicoAtivo(Long medicoId) {
        try {
            ResponseEntity<MedicoRefDTO> response = buscarMedico(medicoId);
            MedicoRefDTO body = response.getBody();
            if (body == null) {
                return false;
            }
            return Boolean.TRUE.equals(body.getAtivo());
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }

    public ResponseEntity<PacienteRefDTO> buscarPaciente(Long id) {
        try {
            return restTemplate.getForEntity(adminUrl + "/v1/pacientes/" + id, PacienteRefDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean isPacienteExistente(Long pacienteId) {
        try {
            ResponseEntity<PacienteRefDTO> response = buscarPaciente(pacienteId);
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }
}
