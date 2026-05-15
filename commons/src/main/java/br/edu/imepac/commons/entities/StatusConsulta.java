package br.edu.imepac.commons.entities;

// fluxo: PENDENTE -> CONFIRMADA -> REALIZADA
// fluxo alternativo: PENDENTE -> CANCELADA (terminal)
public enum StatusConsulta {
    PENDENTE,
    CONFIRMADA,
    REALIZADA,
    CANCELADA
}
