package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Usuario;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.repositories.UsuarioRepository;
import br.com.hw.hwatendimento.util.Navegacao;
import br.com.hw.hwatendimento.util.Sessao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class UsuarioController {
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colNome, colAdmin, colAtivo;
    @FXML private TextField txtNome;
    @FXML private PasswordField txtSenha;
    @FXML private CheckBox chkAdmin;
    @FXML private Label lblMensagem;
    @FXML private Button btnCadastrar, btnAlternarAtivo;

    private void mensagem(String mensagem, String classe){
        lblMensagem.getStyleClass().removeAll("mensagemSucesso", "mensagemErro", "mensagemCarregando");
        lblMensagem.setText(mensagem);
        lblMensagem.getStyleClass().add(classe);
    }

    // transforma verdadeiro/falso em "sim"/"nao" para mostrar na tabela
    private String simNao(boolean valor) {
        if (valor) {
            return "Sim";
        }
        return "Não";
    }

    @FXML
    private void initialize() {
        colNome.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getNome()));

        colAdmin.setCellValueFactory(dado ->
                new SimpleStringProperty(simNao(dado.getValue().isAdmin())));

        colAtivo.setCellValueFactory(dado ->
                new SimpleStringProperty(simNao(dado.getValue().isAtivo())));

        carregarUsuarios();
    }

    private void carregarUsuarios() {
        Task<List<Usuario>> tarefa = new Task<>() {
            @Override
            protected List<Usuario> call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    UsuarioRepository repo = new UsuarioRepository();
                    return repo.listarTodos(conexao);
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            tblUsuarios.setItems(FXCollections.observableArrayList(tarefa.getValue()));
        });

        tarefa.setOnFailed(e -> {
            mensagem("✖ Erro ao carregar os usuários", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    @FXML
    private void voltar() {
        Navegacao.trocarTela(tblUsuarios, "atendimento-view.fxml");
    }
    @FXML
    private void abrirPesquisa() {
        Navegacao.trocarTela(tblUsuarios, "pesquisa-view.fxml");
    }

    @FXML
    private void cadastrar() {
        String nome = txtNome.getText().trim();
        String senha = txtSenha.getText();

        if (nome.isBlank()) {
            mensagem("✖ Preencha o nome", "mensagemErro");
            return;
        }
        if (senha.isBlank()) {
            mensagem("✖ Preencha a senha", "mensagemErro");
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setSenha(senha);
        usuario.setAdmin(chkAdmin.isSelected());

        mensagem("Salvando...", "mensagemCarregando");
        btnCadastrar.setDisable(true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    UsuarioRepository repo = new UsuarioRepository();
                    repo.inserirUsuario(conexao, usuario);
                }
                return null;
            }
        };

        tarefa.setOnSucceeded(e -> {
            btnCadastrar.setDisable(false);
            txtNome.clear();
            txtSenha.clear();
            chkAdmin.setSelected(false);
            mensagem("✔ Usuário cadastrado", "mensagemSucesso");
            carregarUsuarios();
        });

        tarefa.setOnFailed(e -> {
            btnCadastrar.setDisable(false);

            if (tarefa.getException() instanceof SQLIntegrityConstraintViolationException) {
                mensagem("✖ Já existe um usuário com esse nome", "mensagemErro");
            } else {
                mensagem("✖ Erro ao cadastrar", "mensagemErro");
            }
        });

        new Thread(tarefa).start();
    }

    // alterna qual usuario esta ativo e qual nao esta
    @FXML
    private void alternarAtivo() {
        Usuario selecionado = tblUsuarios.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mensagem("✖ Selecione um usuário na tabela", "mensagemErro");
            return;
        }
        if (selecionado.getId() == Sessao.getUsuario().getId()) {
            mensagem("✖ Você não pode desativar o seu próprio usuário", "mensagemErro");
            return;
        }

        boolean novoAtivo = !selecionado.isAtivo();

        btnAlternarAtivo.setDisable(true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    UsuarioRepository repo = new UsuarioRepository();
                    repo.alterarAtivo(conexao, selecionado.getId(), novoAtivo);
                }
                return null;
            }
        };

        tarefa.setOnSucceeded(e -> {
            btnAlternarAtivo.setDisable(false);

            if (novoAtivo) {
                mensagem("✔ Usuário ativado", "mensagemSucesso");
            } else {
                mensagem("✔ Usuário desativado", "mensagemSucesso");
            }
            carregarUsuarios();
        });

        tarefa.setOnFailed(e -> {
            btnAlternarAtivo.setDisable(false);
            mensagem("✖ Erro ao alterar o usuário", "mensagemErro");
        });

        new Thread(tarefa).start();
    }
}