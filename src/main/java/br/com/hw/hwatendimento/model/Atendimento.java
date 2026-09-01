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

}
