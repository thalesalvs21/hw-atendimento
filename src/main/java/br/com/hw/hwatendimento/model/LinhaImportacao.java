package br.com.hw.hwatendimento.model;

// guarda os dados de uma linha valida da planilha
public class LinhaImportacao {
    private int numeroLinha;
    private String serie;
    private String modelo;
    private String tipo;
    private String nome;
    private String empresa;
    private String telefone;

    public LinhaImportacao(int numeroLinha, String serie, String modelo, String tipo,
                           String nome, String empresa, String telefone) {
        this.numeroLinha = numeroLinha;
        this.serie = serie;
        this.modelo = modelo;
        this.tipo = tipo;
        this.nome = nome;
        this.empresa = empresa;
        this.telefone = telefone;
    }

    public int getNumeroLinha() { return numeroLinha; }
    public String getSerie() { return serie; }
    public String getModelo() { return modelo; }
    public String getTipo() { return tipo; }
    public String getNome() { return nome; }
    public String getEmpresa() { return empresa; }
    public String getTelefone() { return telefone; }
}