package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacaoGoogle;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroAluno;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity(name = "Aluno")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aluno {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCompleto;
    @OneToOne
    @JoinColumn(name = "usuario_sistema_id")
    private Usuario usuarioSistema;
    private String turno;
    private String matricula;
    @OneToOne
    @JoinColumn(name = "role_id")
    private Role role;
    @OneToOne
    private Curso curso;

    public Aluno(DadosCadastroAluno dados, Curso curso, Role role) {
        this.nomeCompleto = dados.nomeCompleto();
        this.usuarioSistema = dados.usuarioSistema();
        this.turno = dados.turno();
        this.matricula = dados.matricula();
        this.curso = curso;
        this.role = role;
    }

    public Aluno(DadosAutenticacaoGoogle data, Role role){
        this.nomeCompleto = data.name();
        this.role = role;

    }

    public void atualizarInformacoes(@Valid DadosAtualizacaoAluno dados, Curso curso) {
        if(dados.nomeCompleto() != null){
            this.nomeCompleto = dados.nomeCompleto();
        }
        if(dados.turno() != null){
            this.turno = dados.turno();
        }
        if(dados.matricula() != null){
            this.matricula = dados.matricula();
        }
        if(dados.curso() != null){
            this.curso = curso;
        }

    }
}