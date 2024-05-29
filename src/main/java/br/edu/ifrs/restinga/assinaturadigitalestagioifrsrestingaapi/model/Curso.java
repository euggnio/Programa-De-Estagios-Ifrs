package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;


import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Curso")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nomeCurso;
    @Column(columnDefinition = "boolean default true")
    private boolean ativo = true;

}
