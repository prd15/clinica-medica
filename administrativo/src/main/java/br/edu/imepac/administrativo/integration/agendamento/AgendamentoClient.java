package br.edu.imepac.administrativo.integration.agendamento;

import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// adapter: dominio depende deste componente, nao do FeignClient diretamente
@Component
public class AgendamentoClient {

    private final AgendamentoFeignClient feignClient;

    public AgendamentoClient(AgendamentoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    // chama /v1/consultas/contagem — payload e so {total}, nao baixa lista.
    // Falha de comunicacao (timeout, 5xx, conexao recusada) propaga como ServicoIndisponivelException
    // -> handler global vira 503. NAO mascarar como 0 — diagnostico errado e silencioso.
    public long contarConsultasPorData(LocalDate data) {
        try {
            var resp = feignClient.contarConsultasPorData(data.toString());
            return resp != null ? resp.getTotal() : 0L;
        } catch (FeignException e) {
            throw new ServicoIndisponivelException(
                    "Agendamento indisponivel ao contar consultas da data " + data, e);
        }
    }
}
