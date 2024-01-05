package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstagiariosRepository extends JpaRepository<Estagiarios,Long> {
}
