package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EstagiariosController extends BaseController{


    @GetMapping("/retornarListaEstagiarios")
    public ResponseEntity listaEstagiarios(){
        try {
            return ResponseEntity.ok(estagiariosRepository.findAll());
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().build();
        }
    }

}
