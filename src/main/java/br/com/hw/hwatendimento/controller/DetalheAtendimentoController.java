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
import java.util.List;
import br.com.hw.hwatendimento.repositories.AuditoriaRepository;
import br.com.hw.hwatendimento.util.Sessao;
import java.sql.SQLException;
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

        // quem esta logado, para o log
        int usuarioId = Sessao.getUsuario().getId();

        mensagem("Salvando...", "mensagemCarregando");
        btnFinalizar.setDisable(true);

        // transacao, para o update e o log irem juntos
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

    // escolhe um arquivo e grava ele no banco
    @FXML
    private void adicionarAnexo() {
        // janela padrao do windows para escolher o arquivo
        FileChooser escolha = new FileChooser();
        escolha.setTitle("Escolher anexo");
        escolha.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF e imagens", "*.pdf", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Todos os arquivos", "*.*")
        );
        File arquivo = escolha.showOpenDialog(btnAdicionarAnexo.getScene().getWindow());

        // a pessoa clicou em cancelar
        if (arquivo == null) {
            return;
        }

        // limite de 20 mb
        if (arquivo.length() > 20L * 1024 * 1024) {
            mensagem("✖ Arquivo maior que 20 MB", "mensagemErro");
            return;
        }

        String nomeOriginal = arquivo.getName();
        int atendimentoId = atendimento.getId();
        int usuarioId = Sessao.getUsuario().getId();

        Anexo anexo = new Anexo();
        anexo.setAtendimentoId(atendimentoId);
        anexo.setNomeArquivo(nomeOriginal);
        anexo.setUsuarioId(usuarioId);

        // o detalhe do log tem limite de 255 letras
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
                // 1. le o arquivo inteiro do pc
                byte[] conteudo = Files.readAllBytes(arquivo.toPath());

                // 2. grava no banco (anexo + log) numa transacao
                Connection conexao = null;
                try {
                    conexao = Conexao.conectar();
                    conexao.setAutoCommit(false);

                    AnexoRepository anexoRepo = new AnexoRepository();
                    anexoRepo.inserirAnexo(conexao, anexo, conteudo);

                    AuditoriaRepository auditoria = new AuditoriaRepository();
                    auditoria.registrar(conexao, usuarioId, "EDICAO", atendimentoId, detalheLog);

                    conexao.commit();

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
            mensagem("✖ Erro ao anexar o arquivo", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    // busca o arquivo no banco, salva numa pasta temporaria do pc e abre
    private void abrirAnexo(Anexo anexo) {
        int anexoId = anexo.getId();
        String nome = anexo.getNomeArquivo();
        mensagem("Abrindo " + nome + "...", "mensagemCarregando");

        Task<Boolean> tarefa = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                byte[] conteudo;
                try (Connection conexao = Conexao.conectar()) {
                    AnexoRepository repo = new AnexoRepository();
                    conteudo = repo.buscarConteudo(conexao, anexoId);
                }

                // anexo sem arquivo no banco
                if (conteudo == null) {
                    return false;
                }

                // pasta temporaria nova, para o arquivo manter o nome original
                Path pasta = Files.createTempDirectory("hwanexo");
                Path arquivo = pasta.resolve(nome);
                Files.write(arquivo, conteudo);

                Desktop.getDesktop().open(arquivo.toFile());
                return true;
            }
        };

        tarefa.setOnSucceeded(e -> {
            if (tarefa.getValue()) {
                mensagem("", "mensagem");
            } else {
                mensagem("✖ Arquivo não encontrado", "mensagemErro");
            }
        });

        tarefa.setOnFailed(e -> {
            mensagem("✖ Não foi possível abrir o arquivo", "mensagemErro");
        });

        new Thread(tarefa).start();
    }
}