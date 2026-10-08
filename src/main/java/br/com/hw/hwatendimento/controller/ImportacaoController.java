package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.model.Equipamento;
import br.com.hw.hwatendimento.model.LinhaImportacao;
import br.com.hw.hwatendimento.repositories.AuditoriaRepository;
import br.com.hw.hwatendimento.repositories.ClienteRepository;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.repositories.EquipamentoRepository;
import br.com.hw.hwatendimento.util.Navegacao;
import br.com.hw.hwatendimento.util.Sessao;
import br.com.hw.hwatendimento.util.ValidadorSerie;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportacaoController {
    @FXML private Label lblArquivo, lblResumo, lblMensagem;
    @FXML private ListView<String> lstErros;
    @FXML private Button btnEscolher, btnImportar;

    private File arquivoEscolhido;

    private void mensagem(String mensagem, String classe){
        lblMensagem.getStyleClass().removeAll("mensagemSucesso", "mensagemErro", "mensagemCarregando");
        lblMensagem.setText(mensagem);
        lblMensagem.getStyleClass().add(classe);
    }

    @FXML
    private void escolherArquivo() {
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Escolher planilha");
        escolha.getExtensionFilters().add(new FileChooser.ExtensionFilter("Planilha CSV", "*.csv"));
        File arquivo = escolha.showOpenDialog(btnEscolher.getScene().getWindow());

        if (arquivo == null) {
            return;
        }

        arquivoEscolhido = arquivo;
        lblArquivo.setText(arquivo.getName());
        btnImportar.setDisable(false);

        // Limpa o resultado de uma importacao anterior
        lstErros.getItems().clear();
        lblResumo.setText("");
        mensagem("", "mensagem");
    }

    @FXML
    private void importar() {
        if (arquivoEscolhido == null) {
            return;
        }

        File arquivo = arquivoEscolhido;
        int usuarioId = Sessao.getUsuario().getId();
        List<LinhaImportacao> validas = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        mensagem("Importando...", "mensagemCarregando");
        btnImportar.setDisable(true);
        btnEscolher.setDisable(true);

        // A tarefa devolve dois numeros: [0] = importados, [1] = ja existiam
        Task<int[]> tarefa = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                // Fase 1: ler e conferir
                List<String> linhas = lerArquivo(arquivo);
                conferirLinhas(linhas, validas, erros);

                // Fase 2: gravar as linhas validas
                return gravarLinhas(validas, erros, usuarioId);
            }
        };

        tarefa.setOnSucceeded(e -> {
            int[] resultado = tarefa.getValue();
            int importados = resultado[0];
            int jaExistiam = resultado[1];

            lstErros.setItems(FXCollections.observableArrayList(erros));
            lblResumo.setText(importados + " importado(s), " + jaExistiam + " já existia(m), " + erros.size() + " com erro");
            mensagem("✔ Importação concluída", "mensagemSucesso");

            // O Importar so volta a funcionar depois de escolher outro arquivo
            btnEscolher.setDisable(false);
        });

        tarefa.setOnFailed(e -> {
            tarefa.getException().printStackTrace();
            mensagem("✖ Erro na importação: " + tarefa.getException().getMessage(), "mensagemErro");
            btnImportar.setDisable(false);
            btnEscolher.setDisable(false);
        });

        new Thread(tarefa).start();
    }

    // grava cada linha valida no banco. cada linha tem a sua propria transacao
    private int[] gravarLinhas(List<LinhaImportacao> validas, List<String> erros, int usuarioId) throws SQLException {
        int importados = 0;
        int jaExistiam = 0;

        // Agenda dos clientes criados nesta importacao: telefone -> cliente
        Map<String, Cliente> clientesPorTelefone = new HashMap<>();

        ClienteRepository clienteRepo = new ClienteRepository();
        EquipamentoRepository equipamentoRepo = new EquipamentoRepository();

        try (Connection conexao = Conexao.conectar()) {
            conexao.setAutoCommit(false);

            for (LinhaImportacao linha : validas) {
                try {
                    //serie ja cadastrada? Pula
                    if (equipamentoRepo.buscaNumeroSerie(conexao, linha.getSerie()) != null) {
                        jaExistiam++;
                        continue;
                    }

                    // acha o cliente: primeiro na agenda desta importacao, depois no banco
                    boolean clienteNovo = false;
                    Cliente cliente = clientesPorTelefone.get(linha.getTelefone());
                    if (cliente == null) {
                        cliente = clienteRepo.buscaPorTelefone(conexao, linha.getTelefone());
                    }
                    if (cliente == null) {
                        cliente = new Cliente();
                        cliente.setTipo(linha.getTipo());
                        cliente.setNome(linha.getNome());
                        cliente.setNomeEmpresa(linha.getEmpresa());
                        cliente.setTelefone(linha.getTelefone());
                        clienteRepo.inserirCliente(conexao, cliente);
                        clienteNovo = true;
                    }

                    // cria o equipamento ligado ao cliente
                    Equipamento equipamento = new Equipamento();
                    equipamento.setModelo(linha.getModelo());
                    equipamento.setNumeroSerie(linha.getSerie());
                    equipamento.setCliente(cliente);
                    equipamentoRepo.inserirEquipamento(conexao, equipamento);

                    conexao.commit();
                    importados++;

                    // so entra na agenda depois do commit, quando o cliente existe de verdade no banco
                    if (clienteNovo) {
                        clientesPorTelefone.put(linha.getTelefone(), cliente);
                    }

                } catch (SQLException ex) {
                    // essa linha falhou: desfaz so ela e segue para a proxima
                    conexao.rollback();
                    erros.add("Linha " + linha.getNumeroLinha() + ": erro ao gravar (" + ex.getMessage() + ")");
                }
            }

            // registra a importacao no log
            if (importados > 0) {
                AuditoriaRepository auditoria = new AuditoriaRepository();
                auditoria.registrar(conexao, usuarioId, "CRIACAO", null, "Importou planilha: " + importados + " equipamento(s)");
                conexao.commit();
            }
        }

        return new int[] { importados, jaExistiam };
    }

    // le o arquivo inteiro. Tenta UTF-8; se o Excel salvou no formato antigo, tenta o do Windows
    private List<String> lerArquivo(File arquivo) throws Exception {
        try {
            return Files.readAllLines(arquivo.toPath(), StandardCharsets.UTF_8);
        } catch (MalformedInputException ex) {
            return Files.readAllLines(arquivo.toPath(), Charset.forName("windows-1252"));
        }
    }

    // confere cada linha: as boas vao para "validas", os problemas para "erros"
    private void conferirLinhas(List<String> linhas, List<LinhaImportacao> validas, List<String> erros) {
        if (linhas.isEmpty()) {
            erros.add("O arquivo está vazio.");
            return;
        }


        String cabecalho = linhas.get(0);
        if (cabecalho.startsWith("\uFEFF")) {
            cabecalho = cabecalho.substring(1);
        }

        // Descobre o separador pelo cabecalho ; ou ,
        String separador = ",";
        if (cabecalho.contains(";")) {
            separador = ";";
        }

        Set<String> seriesNaPlanilha = new HashSet<>();

        // Comeca do 1 para pular o cabecalho
        for (int i = 1; i < linhas.size(); i++) {
            int numeroLinha = i + 1; // numero da linha como aparece no Excel
            String linha = linhas.get(i);

            // Pula linhas vazias
            if (linha.replace(separador, "").isBlank()) {
                continue;
            }

            String[] colunas = linha.split(separador, -1);
            if (colunas.length < 5) {
                erros.add("Linha " + numeroLinha + ": faltam colunas (encontradas " + colunas.length + " de 5)");
                continue;
            }

            String serie = limpar(colunas[0]).toUpperCase();
            String tipo = limpar(colunas[1]).toUpperCase();
            String nome = limpar(colunas[2]);
            String empresa = limpar(colunas[3]);
            String telefone = limpar(colunas[4]).replaceAll("\\D", "");

            if (!ValidadorSerie.validar(serie)) {
                erros.add("Linha " + numeroLinha + ": número de série inválido (" + serie + ")");
                continue;
            }
            if (seriesNaPlanilha.contains(serie)) {
                erros.add("Linha " + numeroLinha + ": série " + serie + " repetida na planilha");
                continue;
            }
            if (!tipo.equals("F") && !tipo.equals("J")) {
                erros.add("Linha " + numeroLinha + ": tipo deve ser F ou J (veio \"" + tipo + "\")");
                continue;
            }
            if (nome.isBlank()) {
                erros.add("Linha " + numeroLinha + ": nome do contato em branco");
                continue;
            }
            if (tipo.equals("J") && empresa.isBlank()) {
                erros.add("Linha " + numeroLinha + ": cliente J sem nome da empresa");
                continue;
            }
            if (telefone.length() < 10 || telefone.length() > 11) {
                erros.add("Linha " + numeroLinha + ": telefone inválido, precisa de DDD + número");
                continue;
            }

            // Pessoa fisica nao tem empresa, mesmo que tenham preenchido
            if (tipo.equals("F")) {
                empresa = null;
            }

            seriesNaPlanilha.add(serie);
            String modelo = ValidadorSerie.modeloPorSerie(serie);
            validas.add(new LinhaImportacao(numeroLinha, serie, modelo, tipo, nome, empresa, telefone));
        }
    }

    // Tira espacos das pontas e as aspas que o Excel as vezes coloca em volta do texto
    private String limpar(String texto) {
        String resultado = texto.trim();
        if (resultado.startsWith("\"") && resultado.endsWith("\"") && resultado.length() >= 2) {
            resultado = resultado.substring(1, resultado.length() - 1).trim();
        }
        return resultado;
    }

    @FXML
    private void voltar() {
        Navegacao.trocarTela(lblArquivo, "atendimento-view.fxml");
    }

    @FXML
    private void abrirUsuarios() {
        Navegacao.trocarTela(lblArquivo, "usuario-view.fxml");
    }
}