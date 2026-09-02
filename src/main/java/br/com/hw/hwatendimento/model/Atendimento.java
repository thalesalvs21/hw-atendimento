package br.com.hw.hwatendimento.model;

import java.time.LocalDateTime;

public class Atendimento {

    private int id;
    private Equipamento equipamento;
    private Cliente cliente;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private String descricao;
    private LocalDateTime criadoEm;

    public Atendimento(){
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public Equipamento getEquipamento(){
        return equipamento;
    }
    public void setEquipamento(Equipamento equipamento){
        this.equipamento = equipamento;
    }

    public Cliente getCliente(){
        return cliente;
    }
    public void setCliente(Cliente cliente){
        this.cliente = cliente;
    }

    public LocalDateTime getDataHoraInicio(){
        return dataHoraInicio;
    }
    public void setDataHoraInicio(LocalDateTime dataHoraInicio){
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }
    public void setDataHoraFim(LocalDateTime dataHoraFim){
        this.dataHoraFim = dataHoraFim;
    }

    public String getDescricao(){
        return descricao;
    }
    public void setDescricao(String descricao){
        this.descricao = descricao;
    }

    public LocalDateTime getCriadoEm(){
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm){
        this.criadoEm = criadoEm;
    }
}
