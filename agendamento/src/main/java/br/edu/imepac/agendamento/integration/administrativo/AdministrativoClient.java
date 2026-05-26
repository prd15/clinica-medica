package br.edu.imepac.agendamento.integration.administrativo;

import br.edu.imepac.agendamento.integration.administrativo.dto.ConvenioRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.MedicoRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.PacienteRefDTO;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.function.Predicate;

@Component
public class AdministrativoClient {

    private final RestTemplate restTemplate;
    private final String adminUrl;

    public AdministrativoClient(RestTemplate restTemplate,
                                @Value("${administrativo.url}") String adminUrl) {
        this.restTemplate = restTemplate;
        this.adminUrl = adminUrl;
    }

    public Optional<ConvenioRefDTO> buscarConvenio(Long id) {
        return buscar("/v1/convenios/" + id, ConvenioRefDTO.class);
    }

    public boolean isConvenioAtivo(Long convenioId) {
        return isAtivo(buscarConvenio(convenioId), ConvenioRefDTO::getAtivo);
    }

    public Optional<MedicoRefDTO> buscarMedico(Long id) {
        return buscar("/v1/medicos/" + id, MedicoRefDTO.class);
    }

    public boolean isMedicoAtivo(Long medicoId) {
        return isAtivo(buscarMedico(medicoId), MedicoRefDTO::getAtivo);
    }

    public Optional<PacienteRefDTO> buscarPaciente(Long id) {
        return buscar("/v1/pacientes/" + id, PacienteRefDTO.class);
    }

    // paciente nao tem campo ativo — basta existir
    public boolean isPacienteExistente(Long pacienteId) {
        return buscarPaciente(pacienteId).isPresent();
    }

    // helper unico para os 3 buscarX:
    //   404 -> Optional.empty() (recurso nao existe, regra de negocio)
    //   timeout/5xx/conexao recusada -> ServicoIndisponivelException -> handler vira 503
    private <T> Optional<T> buscar(String path, Class<T> tipo) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(adminUrl + path, tipo));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ServicoIndisponivelException(
                    "Administrativo indisponivel ao chamar " + path, e);
        }
    }

    // recurso existe E atende ao predicado de ativo
    private <T> boolean isAtivo(Optional<T> recurso, Predicate<T> ativo) {
        return recurso.map(ativo::test).orElse(false);
    }
}
