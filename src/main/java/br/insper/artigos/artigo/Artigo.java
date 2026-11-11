package br.insper.artigos.artigo;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Artigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    @NotBlank(message = "O email do avaliado é obrigatório")
    private String email;

    @Column(length = 1000)
    @JsonAlias({"descricaoAvaliacao", "descricao"})
    private String descricao;

    @NotNull(message = "A prioridade é obrigatória")
    @Min(value = 0, message = "A prioridade mínima é 0")
    @Max(value = 10, message = "A prioridade máxima é 10")
    private Integer prioridade;

    public Artigo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // TITULO
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // EMAIl

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // DESCRICAO
    public String getdescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    //PRIORIDADE
    public Integer getPrioridade() {
        return prioridade;
    }

    public void setNota(Integer prioridade) {
        this.prioridade = prioridade;
    }

}
