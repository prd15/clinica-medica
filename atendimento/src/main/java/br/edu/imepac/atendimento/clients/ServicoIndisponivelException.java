package br.edu.imepac.atendimento.clients;

// lancada quando um servico externo (ex: agendamento) esta fora do ar ou retornou erro
// que nao seja 404 — distinguimos do "recurso nao encontrado" para diagnostico correto
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String message) {
        super(message);
    }

    public ServicoIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
