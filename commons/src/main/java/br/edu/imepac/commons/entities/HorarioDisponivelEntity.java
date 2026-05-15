package br.edu.imepac.commons.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "horarios_disponiveis")
public class HorarioDisponivelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // medico vive no banco do administrativo — referencia por id, sem FK
    @Column(nullable = false)
    private Long medicoId;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    // slot livre por padrao; vira true quando uma consulta ocupa o horario
    @Column(nullable = false)
    private Boolean ocupado = false;
}
