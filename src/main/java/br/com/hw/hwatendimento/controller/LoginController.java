package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Usuario;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.repositories.UsuarioRepository;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import br.com.hw.hwatendimento.HWApplication;
import br.com.hw.hwatendimento.util.Sessao;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
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
        //o bindBidiretional ele conecta os dois textos (visivel e invisivel) logo, o entrar continua lendo so do txtSenha, e funciona mesmo se a pessoa digitou no campo visivel
        txtSenhaVisivel.textProperty().bindBidirectional(txtSenha.textProperty());
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
        Usuario escolhido = cmbUsuario.getValue();
        String senha = txtSenha.getText();

        if (escolhido == null) {
            mensagem("✖ Selecione o usuário", "mensagemErro");
            return;
        }
        if (senha.isBlank()) {
            mensagem("✖ Digite a senha", "mensagemErro");
            return;
        }

        mensagem("Entrando...", "mensagemCarregando");
        btnOk.setDisable(true);

        Task<Usuario> tarefa = new Task<>() {
            @Override
            protected Usuario call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    UsuarioRepository repo = new UsuarioRepository();
                    return repo.autenticar(conexao, escolhido.getId(), senha);
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            btnOk.setDisable(false);
            Usuario usuario = tarefa.getValue();

            if (usuario == null) {
                mensagem("✖ Senha incorreta", "mensagemErro");
                txtSenha.clear();
                return;
            }

            Sessao.entrar(usuario);
            abrirTelaPrincipal();
        });

        tarefa.setOnFailed(e -> {
            btnOk.setDisable(false);
            mensagem("✖ Erro ao conectar no banco", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    private void abrirTelaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(HWApplication.class.getResource("atendimento-view.fxml"));
            Stage janela = new Stage();
            janela.setScene(new Scene(loader.load()));
            janela.setTitle("HW Atendimento");
            janela.getIcons().addAll(
                    new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-16.png")),
                    new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-32.png")),
                    new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-48.png")),
                    new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-256.png"))
            );
            janela.show();

            // fecha a janela de login
            Stage login = (Stage) btnOk.getScene().getWindow();
            login.close();

        } catch (IOException ex) {
            mensagem("✖ Erro ao abrir a tela principal", "mensagemErro");
        }
    }

    @FXML
    private void cancelar() {
        Platform.exit();
    }

    @FXML
    private void alternarSenha() {
        boolean mostrando = txtSenhaVisivel.isVisible();

        txtSenhaVisivel.setVisible(!mostrando);
        txtSenhaVisivel.setManaged(!mostrando);
        txtSenha.setVisible(mostrando);
        txtSenha.setManaged(mostrando);

        if (mostrando) {
            txtSenha.requestFocus();
            txtSenha.end();
        } else {
            txtSenhaVisivel.requestFocus();
            txtSenhaVisivel.end();
        }
    }
}