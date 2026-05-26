package br.edu.imepac.atendimento.clients.dto;

// copia local do StatusConsulta do agendamento — bounded context: cada servico tem seu vocabulario
public enum StatusConsulta {
    PENDENTE,
    CONFIRMADA,
    REALIZADA,
    CANCELADA
}
