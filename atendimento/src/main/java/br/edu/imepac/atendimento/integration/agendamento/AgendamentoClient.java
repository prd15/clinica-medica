package br.edu.imepac.atendimento.integration.agendamento;

import br.edu.imepac.atendimento.integration.agendamento.dto.ConsultaRefDTO;
import br.edu.imepac.atendimento.outbox.EventoPermanenteException;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.Optional;

// adapter: dominio depende deste componente, nao do FeignClient diretamente
@Component
public class AgendamentoClient {

    private final AgendamentoFeignClient feignClient;

    public AgendamentoClient(AgendamentoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    // busca a consulta no agendamento antes de registrar o atendimento
    // 404 -> Optional.empty() (consulta nao existe); qualquer outra falha de comunicacao
    // (timeout, 5xx, conexao recusada) -> ServicoIndisponivelException -> handler vira 503
    public Optional<ConsultaRefDTO> buscarConsulta(Long consultaId) {
        try {
            return Optional.ofNullable(feignClient.buscarConsulta(consultaId));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            throw new ServicoIndisponivelException(
                    "Agendamento indisponivel ao buscar consulta " + consultaId, e);
        }
    }

    // notifica o agendamento que a consulta foi realizada. Chamado pelo OutboxScheduler.
    //
    // Tratamento de erros (importante pra evitar retry infinito):
    //   404 NotFound -> consulta sumiu do agendamento, retry nao vai ressuscitar -> PERMANENTE
    //   409 Conflict -> consulta ja esta em status terminal (REALIZADA/CANCELADA), idempotente -> PERMANENTE
    //   demais (timeout, 5xx, connection refused) -> propaga como FeignException -> outbox conta como falha transitoria
    public void confirmarRealizacao(Long consultaId) {
        try {
            feignClient.confirmarRealizacao(consultaId);
        } catch (FeignException.NotFound e) {
            throw new EventoPermanenteException(
                    "Consulta " + consultaId + " nao existe mais no agendamento (404)", e);
        } catch (FeignException.Conflict e) {
            throw new EventoPermanenteException(
                    "Consulta " + consultaId + " ja esta em status terminal (409)", e);
        }
    }
}
