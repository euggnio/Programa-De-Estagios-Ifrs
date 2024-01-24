package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstagiariosRepository extends JpaRepository<Estagiarios,Long> {


    @Query( value = "SELECT * FROM estagiarios LIMIT 20 OFFSET :pagina", nativeQuery = true)
    List<Estagiarios> pegarPagina(int pagina);



}
