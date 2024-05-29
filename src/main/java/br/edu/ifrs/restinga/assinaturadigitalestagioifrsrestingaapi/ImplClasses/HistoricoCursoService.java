package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.HistoricoCursoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.HistoricoCurso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoCursoService {

    @Autowired
    private HistoricoCursoRepository historicoCursoRepository;


    public void salvarCadastroServidor(Curso curso, String nomeServidor){
        HistoricoCurso historicoCurso = new HistoricoCurso(curso,nomeServidor,"Cadastro");
        historicoCursoRepository.save(historicoCurso);
    }

    public void salvarDesligamentoServidor(Curso curso, String nomeServidor){
        HistoricoCurso historicoCurso = new HistoricoCurso(curso,nomeServidor,"Desligamento");
        historicoCursoRepository.save(historicoCurso);
    }

    public List<HistoricoCurso> retornarHistoricoCurso(){
        return historicoCursoRepository.findAll();
    }



}
