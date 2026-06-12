package br.edu.imepac.agendamento.consulta;

// fluxo: PENDENTE -> CONFIRMADA -> REALIZADA
// fluxo alternativo: PENDENTE -> CANCELADA (terminal)
public enum StatusConsulta {
    PENDENTE,
    CONFIRMADA,
    REALIZADA,
    CANCELADA
}
