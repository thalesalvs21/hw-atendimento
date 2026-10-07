package br.com.hw.hwatendimento.model;

import java.time.LocalDateTime;

public class Auditoria {
    private int id;
    private int usuarioId;
    private String usuarioNome;
    private String acao;
    private Integer atendimentoId;
    private String detalhe;
    private LocalDateTime criadoEm;

    public Auditoria(){
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public int getUsuarioId(){
        return usuarioId;
    }
    public void setUsuarioId(int usuarioId){
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome(){
        return usuarioNome;
    }
    public void setUsuarioNome(String usuarioNome){
        this.usuarioNome = usuarioNome;
    }

    public String getAcao(){
        return acao;
    }
    public void setAcao(String acao){
        this.acao = acao;
    }

    public Integer getAtendimentoId(){
        return atendimentoId;
    }
    public void setAtendimentoId(Integer atendimentoId){
        this.atendimentoId = atendimentoId;
    }

    public String getDetalhe(){
        return detalhe;
    }
    public void setDetalhe(String detalhe){
        this.detalhe = detalhe;
    }

    public LocalDateTime getCriadoEm(){
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm){
        this.criadoEm = criadoEm;
    }
}