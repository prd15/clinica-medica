package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.HorarioDisponivelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivelEntity, Long> {

    // todos os slots cadastrados de um medico — independente de ocupado/livre
    List<HorarioDisponivelEntity> findByMedicoId(Long medicoId);

    // util pro front mostrar so os horarios livres no momento de agendar
    List<HorarioDisponivelEntity> findByMedicoIdAndOcupado(Long medicoId, Boolean ocupado);
}
