package br.com.hw.hwatendimento.model;

import java.time.LocalDateTime;

public class Usuario {
    private int id;
    private String nome;
    private String senha;
    private boolean admin;
    private boolean ativo;
    private LocalDateTime criadoEm;

    public Usuario(){
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getNome(){
        return nome;
    }
    public void setNome(String nome){
        this.nome = nome;
    }

    public String getSenha(){
        return senha;
    }
    public void setSenha(String senha){
        this.senha = senha;
    }

    public boolean isAdmin(){
        return admin;
    }
    public void setAdmin(boolean admin){
        this.admin = admin;
    }

    public boolean isAtivo(){
        return ativo;
    }
    public void setAtivo(boolean ativo){
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm(){
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm){
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {
        return nome;
    }
}