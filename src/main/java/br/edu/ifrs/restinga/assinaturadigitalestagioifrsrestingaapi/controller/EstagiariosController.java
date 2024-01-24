package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
//Limitar para servidores!!!!
public class EstagiariosController extends BaseController{


    @GetMapping("/retornarListaEstagiarios")
    public ResponseEntity listaEstagiarios(){
        try {
            return ResponseEntity.ok(estagiariosRepository.findAll());
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping()
    public ResponseEntity buscarPorNome(){
        try {
            return ResponseEntity.ok(estagiariosRepository.findAll());
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/retornarListaEstagiariosPagina")
    public ResponseEntity listaEstagiariosPagina(@RequestParam int pagina){
        try {
            return ResponseEntity.ok(estagiariosRepository.pegarPagina(pagina * 20));
        }catch (RuntimeException e){
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
