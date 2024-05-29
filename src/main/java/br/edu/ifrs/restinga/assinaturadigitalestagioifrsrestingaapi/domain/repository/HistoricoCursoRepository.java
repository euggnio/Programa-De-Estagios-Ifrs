package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.HistoricoCurso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoCursoRepository extends JpaRepository<HistoricoCurso, Long> {
}
