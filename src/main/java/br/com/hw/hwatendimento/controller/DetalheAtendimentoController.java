package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Atendimento;
import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.model.Equipamento;
import br.com.hw.hwatendimento.util.Mascaras;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import br.com.hw.hwatendimento.repositories.AtendimentoRepository;
import br.com.hw.hwatendimento.repositories.Conexao;
import javafx.concurrent.Task;
import java.sql.Connection;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import br.com.hw.hwatendimento.model.Anexo;
import br.com.hw.hwatendimento.repositories.AnexoRepository;
import javafx.collections.FXCollections;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import br.com.hw.hwatendimento.repositories.AuditoriaRepository;
import br.com.hw.hwatendimento.util.Sessao;
import java.sql.SQLException;
import javafx.application.Platform;
import java.awt.Desktop;

public class DetalheAtendimentoController {
    @FXML private Label lblStatus, lblMensagem;
    @FXML private TextField txtCliente, txtTelefone, txtSerie, txtModelo, txtInicio, txtHoraFim;
    @FXML private DatePicker dtFim;
    @FXML private TextArea txtDescricao;
    @FXML private Button btnFinalizar;
    @FXML private ListView<Anexo> lstAnexos;
    @FXML private Button btnAdicionarAnexo;

    private Atendimento atendimento;
    private boolean finalizado = false;

    // a tela de cadastro pergunta isso depois que a janela fecha, para saber se precisa recarregar o historico
    public boolean foiFinalizado() {
        return finalizado;
    }

    private void mensagem(String mensagem, String classe){
        lblMensagem.getStyleClass().removeAll("mensagemSucesso", "mensagemErro", "mensagemCarregando");
        lblMensagem.setText(mensagem);
        lblMensagem.getStyleClass().add(classe);
    }

    @FXML
    private void initialize() {
        Mascaras.hora(txtHoraFim);

        // Duplo clique num anexo abre o arquivo
        lstAnexos.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                Anexo selecionado = lstAnexos.getSelectionModel().getSelectedItem();
                if (selecionado != null) {
                    abrirAnexo(selecionado);
                }
            }
        });
    }

    // chamado pela tela de cadastro logo depois de abrir esta janela, para entregar o atendimento clicado
    public void setDados(Atendimento atendimento, Equipamento equipamento) {
        this.atendimento = atendimento;
        Cliente cliente = equipamento.getCliente();

        // Dados fixos
        if ("J".equals(cliente.getTipo())) {
            txtCliente.setText(cliente.getNomeEmpresa() + " (" + cliente.getNome() + ")");
        } else {
            txtCliente.setText(cliente.getNome());
        }
        txtTelefone.setText(cliente.getTelefone());
        txtSerie.setText(equipamento.getNumeroSerie());
        txtModelo.setText(equipamento.getModelo());
        txtInicio.setText(atendimento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        txtDescricao.setText(atendimento.getDescricao());

        if (atendimento.getDataHoraFim() == null) {
            // ja sugere a data e hora de agora, a pessoa ajusta se precisar
            lblStatus.setText("Em aberto");
            lblStatus.getStyleClass().add("seloAberto");

            LocalDateTime agora = LocalDateTime.now();
            dtFim.setValue(agora.toLocalDate());
            txtHoraFim.setText(agora.format(DateTimeFormatter.ofPattern("HH:mm")));

        } else {
            // mostra o fim e trava tudo
            lblStatus.setText("Finalizado");
            lblStatus.getStyleClass().add("seloFinalizado");

            dtFim.setValue(atendimento.getDataHoraFim().toLocalDate());
            txtHoraFim.setText(atendimento.getDataHoraFim().format(DateTimeFormatter.ofPattern("HH:mm")));

            dtFim.setDisable(true);
            txtHoraFim.setEditable(false);
            txtDescricao.setEditable(false);

            btnFinalizar.setVisible(false);
            btnFinalizar.setManaged(false);
        }
        carregarAnexos();
    }

    // busca os anexos do atendimento e mostra na lista
    private void carregarAnexos() {
        int atendimentoId = atendimento.getId();

        Task<List<Anexo>> tarefa = new Task<>() {
            @Override
            protected List<Anexo> call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    AnexoRepository repo = new AnexoRepository();
                    return repo.listarPorAtendimento(conexao, atendimentoId);
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            lstAnexos.setItems(FXCollections.observableArrayList(tarefa.getValue()));
        });

        tarefa.setOnFailed(e -> {
            mensagem("✖ Erro ao carregar os anexos", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    @FXML
    private void fechar() {
        Stage janela = (Stage) btnFinalizar.getScene().getWindow();
        janela.close();
    }

    @FXML
    private void finalizar() {
        if (dtFim.getValue() == null || txtHoraFim.getText().length() != 5) {
            mensagem("✖ Preencha a data e a hora do fim", "mensagemErro");
            return;
        }

        LocalTime horaFim;
        try {
            horaFim = LocalTime.parse(txtHoraFim.getText());
        } catch (DateTimeParseException ex) {
            mensagem("✖ Hora inválida", "mensagemErro");
            return;
        }

        LocalDateTime fim = dtFim.getValue().atTime(horaFim);

        if (fim.isBefore(atendimento.getDataHoraInicio())) {
            mensagem("✖ O fim não pode ser antes do início", "mensagemErro");
            return;
        }
        if (txtDescricao.getText().isBlank()) {
            mensagem("✖ Preencha a descrição", "mensagemErro");
            return;
        }

        // monta so o que vai ser gravado
        Atendimento dados = new Atendimento();
        dados.setId(atendimento.getId());
        dados.setDataHoraFim(fim);
        dados.setDescricao(txtDescricao.getText());

        // NOVO: quem esta logado, para o log
        int usuarioId = Sessao.getUsuario().getId();

        mensagem("Salvando...", "mensagemCarregando");
        btnFinalizar.setDisable(true);

        // agora com transacao, para o update e o log irem juntos
        Task<Boolean> tarefa = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                Connection conexao = null;
                try {
                    conexao = Conexao.conectar();
                    conexao.setAutoCommit(false);

                    AtendimentoRepository repo = new AtendimentoRepository();
                    boolean ok = repo.finalizarAtendimento(conexao, dados);

                    // So registra se realmente finalizou
                    if (ok) {
                        AuditoriaRepository auditoria = new AuditoriaRepository();
                        auditoria.registrar(conexao, usuarioId, "EDICAO", dados.getId(), "Finalizou o atendimento");
                    }

                    conexao.commit();
                    return ok;

                } catch (SQLException e) {
                    if (conexao != null) {
                        try { conexao.rollback(); } catch (SQLException ex) { }
                    }
                    throw e;

                } finally {
                    if (conexao != null) {
                        try { conexao.close(); } catch (SQLException ex) { }
                    }
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            if (tarefa.getValue()) {
                finalizado = true;
                fechar();
            } else {
                // o botao continua travado: nao tem mais o que finalizar
                mensagem("✖ Este atendimento já foi finalizado por outro usuário. Feche e abra de novo para ver.", "mensagemErro");
            }
        });

        tarefa.setOnFailed(e -> {
            btnFinalizar.setDisable(false);
            mensagem("✖ Erro ao finalizar", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    // escolhe um arquivo, copia para a pasta do servidor e grava no banco
    @FXML
    private void adicionarAnexo() {
        String pasta = Conexao.getConfig("anexos.pasta");
        if (pasta == null || pasta.isBlank()) {
            mensagem("✖ Pasta de anexos não configurada (anexos.pasta no config.properties)", "mensagemErro");
            return;
        }

        // Janela padrao do Windows para escolher o arquivo
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Escolher anexo");
        escolha.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF e imagens", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
        File arquivo = escolha.showOpenDialog(btnAdicionarAnexo.getScene().getWindow());

        // A pessoa clicou em Cancelar
        if (arquivo == null) {
            return;
        }

        // Limite de 20 MB
        if (arquivo.length() > 20L * 1024 * 1024) {
            mensagem("✖ Arquivo maior que 20 MB", "mensagemErro");
            return;
        }

        String nomeOriginal = arquivo.getName();
        int atendimentoId = atendimento.getId();
        int usuarioId = Sessao.getUsuario().getId();

        // Nome unico na pasta: id do atendimento + momento atual + nome original
        String nomeUnico = atendimentoId + "_" + System.currentTimeMillis() + "_" + nomeOriginal;
        Path origem = arquivo.toPath();
        Path destino = Paths.get(pasta, nomeUnico);

        Anexo anexo = new Anexo();
        anexo.setAtendimentoId(atendimentoId);
        anexo.setNomeArquivo(nomeOriginal);
        anexo.setCaminho(destino.toString());
        anexo.setUsuarioId(usuarioId);

        // O detalhe do log tem limite de 255 letras
        String detalhe = "Anexou " + nomeOriginal;
        if (detalhe.length() > 255) {
            detalhe = detalhe.substring(0, 255);
        }
        String detalheLog = detalhe;

        mensagem("Enviando arquivo...", "mensagemCarregando");
        btnAdicionarAnexo.setDisable(true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Copia o arquivo para a pasta do servidor
                Files.copy(origem, destino);

                // Grava no banco (anexo + log) numa transacao
                Connection conexao = null;
                try {
                    conexao = Conexao.conectar();
                    conexao.setAutoCommit(false);

                    AnexoRepository anexoRepo = new AnexoRepository();
                    anexoRepo.inserirAnexo(conexao, anexo);

                    AuditoriaRepository auditoria = new AuditoriaRepository();
                    auditoria.registrar(conexao, usuarioId, "EDICAO", atendimentoId, detalheLog);

                    conexao.commit();

                } catch (SQLException e) {
                    if (conexao != null) {
                        try { conexao.rollback(); } catch (SQLException ex) { }
                    }
                    // O banco falhou: apaga o arquivo copiado para nao ficar orfao na pasta
                    Files.deleteIfExists(destino);
                    throw e;

                } finally {
                    if (conexao != null) {
                        try { conexao.close(); } catch (SQLException ex) { }
                    }
                }
                return null;
            }
        };

        tarefa.setOnSucceeded(e -> {
            btnAdicionarAnexo.setDisable(false);
            mensagem("✔ Anexo adicionado", "mensagemSucesso");
            carregarAnexos();
        });

        tarefa.setOnFailed(e -> {
            btnAdicionarAnexo.setDisable(false);
            mensagem("✖ Erro ao anexar o arquivo. Verifique o acesso à pasta de anexos.", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    private void abrirAnexo(Anexo anexo) {
        File arquivo = new File(anexo.getCaminho());
        mensagem("Abrindo " + anexo.getNomeArquivo() + "...", "mensagemCarregando");

        // O Desktop.open pode demorar (arquivo na rede), entao roda fora da tela
        new Thread(() -> {
            try {
                if (!arquivo.exists()) {
                    Platform.runLater(() -> mensagem("✖ Arquivo não encontrado na pasta de anexos", "mensagemErro"));
                    return;
                }
                Desktop.getDesktop().open(arquivo);
                Platform.runLater(() -> mensagem("", "mensagem"));

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> mensagem("✖ Não foi possível abrir o arquivo", "mensagemErro"));
            }
        }).start();
    }

}