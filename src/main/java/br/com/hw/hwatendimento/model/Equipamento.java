package br.com.hw.hwatendimento.model;

import java.time.LocalDateTime;

public class Equipamento {

    private int id;
    private String modelo;
    private String numeroSerie;
    private Cliente cliente;
    private LocalDateTime criadoEm;


    public Equipamento(){
    }
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getModelo(){
        return modelo;
    }
    public void setModelo(String modelo){
        this.modelo = modelo;
    }

    public String getNumeroSerie(){
        return numeroSerie;
    }
    public void setNumeroSerie(String numeroSerie){
        this.numeroSerie = numeroSerie;
    }

    public Cliente getCliente(){
        return cliente;
    }
    public void setCliente(Cliente cliente){
        this.cliente = cliente;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm){
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {
        return modelo;
    }
}