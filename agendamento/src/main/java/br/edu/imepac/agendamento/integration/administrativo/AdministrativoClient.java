package br.edu.imepac.agendamento.integration.administrativo;

import br.edu.imepac.agendamento.integration.administrativo.dto.ConvenioRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.MedicoRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.PacienteRefDTO;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Predicate;

// adapter: dominio depende deste componente, nao do FeignClient diretamente
@Component
public class AdministrativoClient {

    private final AdministrativoFeignClient feignClient;

    public AdministrativoClient(AdministrativoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    public Optional<ConvenioRefDTO> buscarConvenio(Long id) {
        return buscar(() -> feignClient.buscarConvenio(id), "/v1/convenios/" + id);
    }

    public boolean isConvenioAtivo(Long convenioId) {
        return isAtivo(buscarConvenio(convenioId), ConvenioRefDTO::getAtivo);
    }

    public Optional<MedicoRefDTO> buscarMedico(Long id) {
        return buscar(() -> feignClient.buscarMedico(id), "/v1/medicos/" + id);
    }

    public boolean isMedicoAtivo(Long medicoId) {
        return isAtivo(buscarMedico(medicoId), MedicoRefDTO::getAtivo);
    }

    public Optional<PacienteRefDTO> buscarPaciente(Long id) {
        return buscar(() -> feignClient.buscarPaciente(id), "/v1/pacientes/" + id);
    }

    // paciente nao tem campo ativo — basta existir
    public boolean isPacienteExistente(Long pacienteId) {
        return buscarPaciente(pacienteId).isPresent();
    }

    // helper:
    //   404 -> Optional.empty() (recurso nao existe, regra de negocio)
    //   demais FeignException (timeout, 5xx, conexao recusada) -> ServicoIndisponivelException -> handler vira 503
    private <T> Optional<T> buscar(java.util.function.Supplier<T> call, String path) {
        try {
            return Optional.ofNullable(call.get());
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            throw new ServicoIndisponivelException(
                    "Administrativo indisponivel ao chamar " + path, e);
        }
    }

    // recurso existe E atende ao predicado de ativo
    private <T> boolean isAtivo(Optional<T> recurso, Predicate<T> ativo) {
        return recurso.map(ativo::test).orElse(false);
    }
}
