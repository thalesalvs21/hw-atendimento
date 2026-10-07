package br.com.hw.hwatendimento.controller;
import br.com.hw.hwatendimento.util.Mascaras;
import br.com.hw.hwatendimento.util.Navegacao;
import br.com.hw.hwatendimento.util.Sessao;
import br.com.hw.hwatendimento.util.ValidadorSerie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import br.com.hw.hwatendimento.repositories.*;
import br.com.hw.hwatendimento.model.*;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.concurrent.Task;

public class AtendimentoController {
    @FXML private TextField txtNumeroSerie, txtEmpresa, txtNome, txtTelefone, txtHoraFim, txtHoraInicio;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblResultadoBusca, lblHistoricoVazio;
    @FXML private ComboBox<String> cmbModelo;
    @FXML private ToggleGroup tipoCliente;
    @FXML private RadioButton rbJuridica, rbFisica;
    @FXML private DatePicker dtInicio, dtFim;
    @FXML private VBox boxHistorico;
    @FXML private Button btnBuscar, btnSalvar;
    @FXML private Button btnIrUsuarios;
    private List<Atendimento> historicoCarregado;
    private Equipamento equipamentoAtual;

    @FXML
    private void abrirPesquisa() {
        Navegacao.trocarTela(txtNumeroSerie, "pesquisa-view.fxml");
    }
    @FXML
    private void abrirUsuarios() {
        Navegacao.trocarTela(txtNumeroSerie, "usuario-view.fxml");
    }

    private void aplicarModeloPelaSerie(String numeroSerie) {
        String modelo = ValidadorSerie.modeloPorSerie(numeroSerie);

        if (modelo != null) {
            cmbModelo.setValue(modelo);
            cmbModelo.setDisable(true);
        } else {
            cmbModelo.setDisable(false);
        }
    }

    private void mensagem(String mensagem, String classe){
        lblResultadoBusca.getStyleClass().removeAll("mensagemSucesso", "mensagemErro");
        lblResultadoBusca.setText(mensagem);
        lblResultadoBusca.getStyleClass().add(classe);
    }

    private boolean validadorCampo(){
        String numeroSerie = txtNumeroSerie.getText().toUpperCase();
        if (!numeroSerie.isBlank() && !ValidadorSerie.validar(numeroSerie)) {
            mensagem("✖ Número de série inválido!", "mensagemErro");
            return false;
        }
        if (cmbModelo.getValue() == null){
            mensagem("✖ Selecione o modelo corretamente", "mensagemErro");
            return false;
        }
        if (rbJuridica.isSelected() && txtEmpresa.getText().isBlank()){
            mensagem("✖ Preencha o nome da empresa", "mensagemErro");
            return false;
        }
        if (txtNome.getText().isBlank()) {
            mensagem("✖ Preencha o nome", "mensagemErro");
            return false;
        }
        if (txtTelefone.getText().isBlank()){
            mensagem("✖ Preencha o telefone", "mensagemErro");
            return false;
        }
        if (txtDescricao.getText().isBlank()){
            mensagem("✖ Preencha a descrição", "mensagemErro");
            return false;
        }
        if (dtInicio.getValue() == null || txtHoraInicio.getText().length() != 5){
            mensagem("✖ Preencha a data e/ou hora corretamente", "mensagemErro");
            return false;
        }
        return true;
    }

    @FXML
    private void initialize(){
        btnIrUsuarios.setVisible(Sessao.isAdmin());
        btnIrUsuarios.setManaged(Sessao.isAdmin());

        Mascaras.serie(txtNumeroSerie);
        Mascaras.telefone(txtTelefone);
        Mascaras.hora(txtHoraInicio);
        Mascaras.hora(txtHoraFim);

        //Seta as opções da comboBox
        ObservableList<String> opcoes = FXCollections.observableArrayList(
                "ECGV6",
                "ECGV11",
                "Ergo13",
                "ErgoMET13",
                "ErgoCP"
        );
        cmbModelo.setItems(opcoes);

        // Caixa pro nome da empresa começa desativada
        //Depois tem essa verificaçao para ver se a opção PJ ta selecionada, se sim, ela habilita a caixa
        txtEmpresa.setDisable(!rbJuridica.isSelected());

        tipoCliente.selectedToggleProperty().addListener((obs, anterior, novo) -> {
            boolean juridica = rbJuridica.isSelected();

            txtEmpresa.setDisable(!juridica);

            if(!juridica){
                txtEmpresa.clear();
            }
        });
    }

    @FXML
    private void buscarEquipamento() {
        limpaResultado();

        txtNumeroSerie.setText(txtNumeroSerie.getText().toUpperCase());
        String numeroSerie = txtNumeroSerie.getText();

        if (!ValidadorSerie.validar(numeroSerie)){
            mensagem("✖ Número de serie invalido!", "mensagemErro");
            return;
        }
        aplicarModeloPelaSerie(numeroSerie);

        mensagem("Buscando...", "mensagemCarregando");
        btnBuscar.setDisable(true);

        Task<Equipamento> tarefa = new Task<>() {
            @Override
            protected Equipamento call() throws Exception {
                // roda em paralelo, sem travar a tela
                try (Connection conexao = Conexao.conectar()) {
                    EquipamentoRepository eRepo = new EquipamentoRepository();
                    Equipamento encontrado = eRepo.buscaNumeroSerie(conexao, numeroSerie);

                    if (encontrado != null) {
                        AtendimentoRepository aRepo = new AtendimentoRepository();
                        historicoCarregado = aRepo.buscaPorEquipamento(conexao, encontrado.getId());
                    }
                    return encontrado;
                }
            }
        };

        tarefa.setOnSucceeded(e -> {
            btnBuscar.setDisable(false);
            equipamentoAtual = tarefa.getValue();

            if (equipamentoAtual == null) {
                mensagem("✖ Equipamento não encontrado", "mensagemErro");
                return;
            }

            Cliente cliente = equipamentoAtual.getCliente();
            mensagem("✔ Equipamento encontrado", "mensagemSucesso");

            txtNome.setText(cliente.getNome());
            txtTelefone.setText(cliente.getTelefone());

            if ("J".equals(cliente.getTipo())){
                rbJuridica.setSelected(true);
                txtEmpresa.setText(cliente.getNomeEmpresa());
            } else {
                rbFisica.setSelected(true);
                txtEmpresa.setText(null);
            }

            boxHistorico.getChildren().clear();
            lblHistoricoVazio.setVisible(false);
            lblHistoricoVazio.setManaged(false);

            for (Atendimento atendimento : historicoCarregado) {
                Label lblData = new Label(atendimento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                Label lblDescricao = new Label(atendimento.getDescricao());

                VBox bloco = new VBox(lblData, lblDescricao);
                boxHistorico.getChildren().add(bloco);

                lblData.getStyleClass().add("historicoData");
                lblDescricao.getStyleClass().add("historicoTexto");
                lblDescricao.setWrapText(true);
                lblDescricao.setMaxWidth(Double.MAX_VALUE);

                bloco.getStyleClass().add("historicoItem");
            }
        });

        tarefa.setOnFailed(e -> {
            btnBuscar.setDisable(false);
            mensagem("✖ Erro ao consultar o banco", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    private void limpaResultado(){
        txtEmpresa.clear();
        lblResultadoBusca.setText("");
        cmbModelo.getSelectionModel().clearSelection();
        txtNome.clear();
        txtTelefone.clear();
        txtDescricao.clear();
        txtHoraInicio.clear();
        txtHoraFim.clear();
        dtInicio.setValue(null);
        dtFim.setValue(null);
        rbFisica.setSelected(true);
        equipamentoAtual = null;
        boxHistorico.getChildren().clear();
        lblHistoricoVazio.setVisible(true);
        lblHistoricoVazio.setManaged(true);
        cmbModelo.setDisable(false);
    }

    private void limpaTela(){
        limpaResultado();
        txtNumeroSerie.clear();
    }

    @FXML
    private void salvarAtendimento() {
        String numeroSerie = txtNumeroSerie.getText().toUpperCase();

        // Serie preenchida mas ninguem clicou em Buscar: o app consulta sozinho para saber se cria equipamento novo ou nao
        if (equipamentoAtual == null && !numeroSerie.isBlank()) {
            mensagem("Verificando...", "mensagemCarregando");
            btnSalvar.setDisable(true);

            Task<Equipamento> tarefa = new Task<>() {
                @Override
                protected Equipamento call() throws Exception {
                    try (Connection conexao = Conexao.conectar()) {
                        EquipamentoRepository eRepo = new EquipamentoRepository();
                        return eRepo.buscaNumeroSerie(conexao, numeroSerie);
                    }
                }
            };

            tarefa.setOnSucceeded(e -> {
                btnSalvar.setDisable(false);
                equipamentoAtual = tarefa.getValue();

                if (equipamentoAtual != null) {
                    buscarEquipamento();
                    return;
                }
                continuarSalvamento(numeroSerie);
            });

            tarefa.setOnFailed(e -> {
                btnSalvar.setDisable(false);
                mensagem("✖ Erro ao consultar o banco", "mensagemErro");
            });

            new Thread(tarefa).start();
            return;
        }

        continuarSalvamento(numeroSerie);
    }

    private void continuarSalvamento(String numeroSerie) {
        if (!validadorCampo()){
            return;
        }

        LocalDate dataI = dtInicio.getValue();
        LocalTime horaI = LocalTime.parse(txtHoraInicio.getText());
        LocalDateTime inicio = dataI.atTime(horaI);
        LocalDateTime fim = null;

        if (equipamentoAtual != null && !numeroSerie.equals(equipamentoAtual.getNumeroSerie())) {
            mensagem("✖ O número de série mudou. Clique em Buscar novamente", "mensagemErro");
            return;
        }

        if (dtFim.getValue() != null && txtHoraFim.getText().length() == 5) {
            LocalDate dataF = dtFim.getValue();
            LocalTime horaF = LocalTime.parse(txtHoraFim.getText());
            fim = dataF.atTime(horaF);
        }

        // os objetos sao montados aqui, lendo a tela, porque dentro da tarefa nao se pode acessar componente de tela
        Atendimento atendimento = new Atendimento();
        atendimento.setDataHoraInicio(inicio);
        atendimento.setDataHoraFim(fim);
        atendimento.setDescricao(txtDescricao.getText());

        if (equipamentoAtual != null) {
            atendimento.setEquipamento(equipamentoAtual);
            atendimento.setCliente(equipamentoAtual.getCliente());
            gravar(atendimento, null, null);

        } else {
            Cliente cliente = new Cliente();
            cliente.setNome(txtNome.getText());
            cliente.setTipo(rbJuridica.isSelected() ? "J" : "F");
            cliente.setNomeEmpresa(rbJuridica.isSelected() ? txtEmpresa.getText() : null);
            cliente.setTelefone(txtTelefone.getText().replaceAll("\\D", ""));

            Equipamento equipamento = new Equipamento();
            equipamento.setModelo(cmbModelo.getValue());
            equipamento.setNumeroSerie(numeroSerie.isBlank() ? null : numeroSerie);
            equipamento.setCliente(cliente);

            atendimento.setEquipamento(equipamento);
            atendimento.setCliente(cliente);
            gravar(atendimento, cliente, equipamento);
        }
    }

    // Quando cliente e equipamento vem nulos, significa que ja existem no banco e so o atendimento precisa ser inserido
    private void gravar(Atendimento atendimento, Cliente cliente, Equipamento equipamento) {
        mensagem("Salvando...", "mensagemCarregando");
        btnSalvar.setDisable(true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Connection conexao = null;
                try {
                    conexao = Conexao.conectar();
                    // Desliga o gravar-na-hora: os inserts ficam pendentes ate o commit
                    conexao.setAutoCommit(false);

                    if (cliente != null) {
                        ClienteRepository cRepo = new ClienteRepository();
                        cRepo.inserirCliente(conexao, cliente);

                        EquipamentoRepository eRepo = new EquipamentoRepository();
                        eRepo.inserirEquipamento(conexao, equipamento);
                    }

                    AtendimentoRepository aRepo = new AtendimentoRepository();
                    aRepo.inserirAtendimento(conexao, atendimento);

                    conexao.commit();

                } catch (SQLException e) {
                    // Algum insert falhou: desfaz todos
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
            btnSalvar.setDisable(false);
            limpaTela();
            mensagem("✔ Atendimento salvo", "mensagemSucesso");
        });

        tarefa.setOnFailed(e -> {
            btnSalvar.setDisable(false);
            mensagem("✖ Erro ao salvar", "mensagemErro");
        });

        new Thread(tarefa).start();
    }

    @FXML
    private void cancelar() {
        limpaTela();
    }
}
