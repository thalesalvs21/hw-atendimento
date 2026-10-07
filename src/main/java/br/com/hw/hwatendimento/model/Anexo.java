package br.com.hw.hwatendimento.model;

import java.time.LocalDateTime;

public class Anexo {
    private int id;
    private int atendimentoId;
    private String nomeArquivo;
    private String caminho;
    private int usuarioId;
    private LocalDateTime criadoEm;

    public Anexo(){
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public int getAtendimentoId(){
        return atendimentoId;
    }
    public void setAtendimentoId(int atendimentoId){
        this.atendimentoId = atendimentoId;
    }

    public String getNomeArquivo(){
        return nomeArquivo;
    }
    public void setNomeArquivo(String nomeArquivo){
        this.nomeArquivo = nomeArquivo;
    }

    public String getCaminho(){
        return caminho;
    }
    public void setCaminho(String caminho){
        this.caminho = caminho;
    }

    public int getUsuarioId(){
        return usuarioId;
    }
    public void setUsuarioId(int usuarioId){
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getCriadoEm(){
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm){
        this.criadoEm = criadoEm;
    }

    // a lista de anexos na janela de detalhe mostra o nome original do arquivo
    @Override
    public String toString() {
        return nomeArquivo;
    }
}