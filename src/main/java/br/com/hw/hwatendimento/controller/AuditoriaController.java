package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Auditoria;
import br.com.hw.hwatendimento.repositories.AuditoriaRepository;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.util.Navegacao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuditoriaController {
    @FXML private TableView<Auditoria> tblLog;
    @FXML private TableColumn<Auditoria, String> colData, colUsuario, colAcao, colAtendimento, colDetalhe;
    @FXML private Label lblTotal, lblMensagem;
    @FXML private Button btnAtualizar;

    private void mensagem(String mensagem, String classe){
        lblMensagem.getStyleClass().removeAll("mensagemSucesso", "mensagemErro", "mensagemCarregando");
        lblMensagem.setText(mensagem);
        lblMensagem.getStyleClass().add(classe);
    }

    // traduz o codigo do banco para um texto amigavel
    private String nomeAcao(String acao) {
        if (acao.equals("LOGIN")) {
            return "Login";
        }
        if (acao.equals("CRIACAO")) {
            return "Criação";
        }
        if (acao.equals("EDICAO")) {
            return "Edição";
        }
        if (acao.equals("EXCLUSAO")) {
            return "Exclusão";
        }
        return acao;
    }

    @FXML
    private void initialize() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        colData.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getCriadoEm().format(formato)));

        colUsuario.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getUsuarioNome()));

        colAcao.setCellValueFactory(dado ->
                new SimpleStringProperty(nomeAcao(dado.getValue().getAcao())));

        colAtendimento.setCellValueFactory(dado -> {
            Integer id = dado.getValue().getAtendimentoId();
            if (id == null) {
                return new SimpleStringProperty("—");
            }
            return new SimpleStringProperty("#" + id);
        });

        colDetalhe.setCellValueFactory(dado -> {
            String detalhe = dado.getValue().getDetalhe();
            if (detalhe == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(detalhe);
        });

        carregar();
    }

    private void carregar() {
        mensagem("Carregando...", "mensagemCarregando");
        btnAtualizar.setDisable(true);

        Task<List<Auditoria>> tarefa = new Task<>() {
            @Override
            protected List<Auditoria> call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    AuditoriaRepository repo = new AuditoriaRepository();
                    return repo.listar(conexao);
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            List<Auditoria> registros = tarefa.getValue();
            tblLog.setItems(FXCollections.observableArrayList(registros));
            lblTotal.setText(registros.size() + " registro(s)");
            mensagem("", "mensagem");
            btnAtualizar.setDisable(false);
        });

        tarefa.setOnFailed(e -> {
            mensagem("✖ Erro ao carregar o log", "mensagemErro");
            btnAtualizar.setDisable(false);
        });

        new Thread(tarefa).start();
    }

    @FXML
    private void atualizar() {
        carregar();
    }

    @FXML
    private void voltar() {
        Navegacao.trocarTela(tblLog, "atendimento-view.fxml");
    }

    @FXML
    private void abrirUsuarios() {
        Navegacao.trocarTela(tblLog, "usuario-view.fxml");
    }
}