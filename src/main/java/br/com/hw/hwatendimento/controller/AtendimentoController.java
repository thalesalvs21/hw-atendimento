package br.com.hw.hwatendimento.controller;
import br.com.hw.hwatendimento.util.Mascaras;
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

public class AtendimentoController {
    @FXML private TextField txtNumeroSerie, txtEmpresa, txtNome, txtTelefone, txtHoraFim, txtHoraInicio;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblResultadoBusca, lblHistoricoVazio;
    @FXML private ComboBox<String> cmbModelo;
    @FXML private ToggleGroup tipoCliente;
    @FXML private RadioButton rbJuridica, rbFisica;
    @FXML private DatePicker dtInicio, dtFim;
    @FXML private VBox boxHistorico;
    private Equipamento equipamentoAtual;

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

            // Chama o validador de numero de serie
            if (!ValidadorSerie.validar(numeroSerie)){
                mensagem("✖ Número de serie invalido!", "mensagemErro");
                return;
            }

            //Se o numero for valido, puxa os dados do cliente e o historico de atendimento e preenche na tela
            //Se não, devolve uma mensagem de equipamento não encontrado
            try (Connection conexao = Conexao.conectar()) {
                EquipamentoRepository eRepo = new EquipamentoRepository();
                equipamentoAtual = eRepo.buscaNumeroSerie(conexao, numeroSerie);

                if (equipamentoAtual != null) {
                    Cliente cliente = equipamentoAtual.getCliente();

                    mensagem("✔ Equipamento encontrado", "mensagemSucesso");

                    cmbModelo.setValue(equipamentoAtual.getModelo());
                    txtNome.setText(cliente.getNome());
                    txtTelefone.setText(cliente.getTelefone());

                    if ("J".equals(cliente.getTipo())){
                        rbJuridica.setSelected(true);
                        txtEmpresa.setText(cliente.getNomeEmpresa());
                    } else {
                        rbFisica.setSelected(true);
                        txtEmpresa.setText(null);
                    }

                    // Exibir historico de atendimentos
                    boxHistorico.getChildren().clear();
                    lblHistoricoVazio.setVisible(false);
                    lblHistoricoVazio.setManaged(false);

                    AtendimentoRepository aRepo = new AtendimentoRepository();

                    List<Atendimento> historico = aRepo.buscaPorEquipamento(conexao, equipamentoAtual.getId());
                    for (Atendimento atendimento : historico) {
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

                } else {
                    mensagem("✖ Equipamento não encontrado", "mensagemErro");
                    }
            }
            catch (SQLException e) {
                mensagem("✖ Erro ao consultar o banco", "mensagemErro");
            }
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
    }

    private void limpaTela(){
        limpaResultado();
        txtNumeroSerie.clear();
    }
    @FXML
    private void salvarAtendimento() {
        String numeroSerie = txtNumeroSerie.getText().toUpperCase();
        if (equipamentoAtual == null && !numeroSerie.isBlank()) {
            buscarEquipamento();

            if (equipamentoAtual != null) {
                mensagem("✖ Esse equipamento já tem cliente vinculado. Confira os dados e salve novamente", "mensagemErro");
                return;
            }
        }

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

        if (equipamentoAtual != null) {
            Atendimento atendimento = new Atendimento();
            atendimento.setEquipamento(equipamentoAtual);
            atendimento.setCliente(equipamentoAtual.getCliente());
            atendimento.setDataHoraInicio(inicio);
            atendimento.setDataHoraFim(fim);
            atendimento.setDescricao(txtDescricao.getText());

            try (Connection conexao = Conexao.conectar()) {
                AtendimentoRepository aRepo = new AtendimentoRepository();
                aRepo.inserirAtendimento(conexao, atendimento);

                limpaTela();
                mensagem("✔ Atendimento salvo", "mensagemSucesso");
            } catch (SQLException e) {
                mensagem("✖ Erro ao salvar o atendimento", "mensagemErro");
            }
        }

    }

    @FXML
    private void cancelar() {
        limpaTela();
    }
}
