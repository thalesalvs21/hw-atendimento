package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Usuario;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.repositories.UsuarioRepository;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.util.List;

public class LoginController {
    @FXML private ComboBox<Usuario> cmbUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private TextField txtSenhaVisivel;
    @FXML private Label lblMensagem;
    @FXML private Button btnOk;

    private void mensagem(String mensagem, String classe){
        lblMensagem.getStyleClass().removeAll("mensagemSucesso", "mensagemErro", "mensagemCarregando");
        lblMensagem.setText(mensagem);
        lblMensagem.getStyleClass().add(classe);
    }

    @FXML
    private void initialize() {
        mensagem("Carregando usuários...", "mensagemCarregando");
        btnOk.setDisable(true);

        Task<List<Usuario>> tarefa = new Task<>() {
            @Override
            protected List<Usuario> call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    UsuarioRepository repo = new UsuarioRepository();
                    return repo.listarAtivos(conexao);
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            cmbUsuario.setItems(FXCollections.observableArrayList(tarefa.getValue()));
            mensagem("", "mensagem");
            btnOk.setDisable(false);
        });

        tarefa.setOnFailed(e -> {
            mensagem("✖ Erro ao conectar no banco", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    @FXML
    private void entrar() {
    }

    @FXML
    private void cancelar() {
    }

    @FXML
    private void alternarSenha() {
    }
}