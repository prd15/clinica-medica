package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.MedicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoEntity, Long> {

    Optional<MedicoEntity> findByCrm(String crm);

    List<MedicoEntity> findByAtivo(Boolean ativo);
}
