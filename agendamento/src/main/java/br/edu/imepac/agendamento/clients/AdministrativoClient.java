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

import java.util.function.Predicate;
import java.util.function.Supplier;

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
        return consultarAtivo(() -> buscarConvenio(convenioId), ConvenioRefDTO::getAtivo);
    }

    public ResponseEntity<MedicoRefDTO> buscarMedico(Long id) {
        try {
            return restTemplate.getForEntity(adminUrl + "/v1/medicos/" + id, MedicoRefDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean isMedicoAtivo(Long medicoId) {
        return consultarAtivo(() -> buscarMedico(medicoId), MedicoRefDTO::getAtivo);
    }

    public ResponseEntity<PacienteRefDTO> buscarPaciente(Long id) {
        try {
            return restTemplate.getForEntity(adminUrl + "/v1/pacientes/" + id, PacienteRefDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        }
    }

    // paciente nao tem campo ativo — basta existir (corpo presente)
    public boolean isPacienteExistente(Long pacienteId) {
        return consultarAtivo(() -> buscarPaciente(pacienteId), p -> true);
    }

    // fail-safe: qualquer falha de comunicacao (404, 5xx, timeout, conexao recusada) retorna false.
    // melhor um falso negativo do que agendar contra um recurso invalido ou indisponivel.
    private <T> boolean consultarAtivo(Supplier<ResponseEntity<T>> chamada, Predicate<T> ativo) {
        try {
            T body = chamada.get().getBody();
            return body != null && ativo.test(body);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (RestClientException e) {
            return false;
        }
    }
}
