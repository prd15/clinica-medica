package br.edu.imepac.commons.exceptions;

// Lancada quando um microsservico externo esta fora do ar ou retornou erro
// que NAO seja 404 (timeout, 5xx, conexao recusada). Distingue do "recurso
// nao encontrado" para diagnostico correto — handler global mapeia para 503.
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String message) {
        super(message);
    }

    public ServicoIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
