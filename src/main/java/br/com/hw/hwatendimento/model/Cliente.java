package br.com.hw.hwatendimento.model;

import java.time.LocalDateTime;

public class Cliente {

    private int id;
    private String tipo;
    private String nome;
    private String nomeEmpresa;
    private String telefone;
    private LocalDateTime criadoEm;

    public Cliente(){
    }
    public String getTipo(){
        return tipo;
    }
    public void setTipo(String tipo){
        this.tipo = tipo;
    }

    public String getNome(){
        return nome;
    }
    public void setNome(String nome){
        this.nome = nome;
    }

    public String getNomeEmpresa(){
        return nomeEmpresa;
    }
    public void setNomeEmpresa(String nomeEmpresa){
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getTelefone(){
        return telefone;
    }
    public void setTelefone(String telefone){
        this.telefone = telefone;
    }

    public LocalDateTime getCriadoEm(){
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm){
        this.criadoEm = criadoEm;
    }
}
