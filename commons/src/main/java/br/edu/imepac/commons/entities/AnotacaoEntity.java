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
@Table(name = "anotacoes")
public class AnotacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long prontuarioId;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private LocalDateTime dataCriacao;
}
