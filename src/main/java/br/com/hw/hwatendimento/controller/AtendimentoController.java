package br.com.hw.hwatendimento.controller;
import javafx.application.Platform;
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

    //Criação do metodo para mascara da hora
    private void mascaraHora(TextField campo){
        campo.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (mudanca.isDeleted()) {
                return mudanca;
            }

            String texto = mudanca.getControlNewText();
            String digitos = texto.replaceAll("\\D", "");
            String formatado;


            if (digitos.length() > 4) {
                digitos = digitos.substring(0, 4);
            }

            if (digitos.length() <= 2) {
                formatado = digitos;
            } else {
                formatado = digitos.substring(0, 2) + ":" + digitos.substring(2);
            }
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.setCaretPosition(formatado.length());
            mudanca.setAnchor(formatado.length());

            return mudanca;
        }));
    }

    @FXML
    private void initialize(){
        //Limita a caixa de texto em 11 caracteres
        txtNumeroSerie.setTextFormatter(new TextFormatter<String>(tamanho -> {
            if (tamanho.getControlNewText().length() <= 11) {
                return tamanho;
            } return null;
        }));

        //Formata o telefone com a mascara (--) 11111-1111
        txtTelefone.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (mudanca.isDeleted()) {
                return mudanca;
            }

            String texto = mudanca.getControlNewText();
            String digitos = texto.replaceAll("\\D", "");
            String formatado;


            if (digitos.length() > 11) {
                digitos = digitos.substring(0, 11);
            }

            if (digitos.length() <= 2) {
                formatado = digitos;
            } else if (digitos.length() <= 7) {
                formatado = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2);
            } else {
                formatado = "(" + digitos.substring(0, 2) + ") "
                + digitos.substring(2, 7) + "-"
                + digitos.substring(7);
            }
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.setCaretPosition(formatado.length());
            mudanca.setAnchor(formatado.length());
            return mudanca;
        }));
        //Chamando metodo mascara hora
        mascaraHora(txtHoraInicio);
        mascaraHora(txtHoraFim);

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
        txtEmpresa.setDisable(!rbJuridica.isSelected());
        //Depois tem essa verificaçao para ver se a opção PJ ta selecionada, se sim, ela habilita a caixa
        tipoCliente.selectedToggleProperty().addListener((obs, anterior, novo) -> {
            boolean juridica = rbJuridica.isSelected();

            txtEmpresa.setDisable(!juridica);

            if(!juridica){
                txtEmpresa.clear();
            }
        });
    }

    private void retornaInvalido(){
        lblResultadoBusca.setText("✖ Número de serie invalido!");
        lblResultadoBusca.getStyleClass().add("mensagemErro");
    }

    @FXML
    private void buscarEquipamento() {
        lblResultadoBusca.getStyleClass().removeAll("mensagemSucesso", "mensagemErro");
        txtNumeroSerie.setText(txtNumeroSerie.getText().toUpperCase());
        String numeroSerie = txtNumeroSerie.getText();

        if (!numeroSerie.matches("[A-Z]{2}\\d{9}")) {
            retornaInvalido();
            return;
        }

        String modelo = numeroSerie.substring(0, 2);
        String versao = numeroSerie.substring(2, 4);
        int ano = Integer.parseInt(numeroSerie.substring(4, 6));
        int anoAtual = LocalDate.now().getYear() % 100;
        int mes = Integer.parseInt(numeroSerie.substring(6, 8));
        int numeroProducao = Integer.parseInt(numeroSerie.substring(8, 11));

        //Valida se o numero de serie é valido
        if (!modelo.equals("TC") && !modelo.equals("TE") && !modelo.equals("EC") && !modelo.equals("TP")) {
            retornaInvalido();
            return;
        }
        if (!versao.equals("10") && !(versao.equals("11") && modelo.equals("EC"))) {
            retornaInvalido();
            return;
        }
        if (ano < 9 || ano > anoAtual) {
            retornaInvalido();
            return;
        }
        if (mes < 1 || mes > 12) {
            retornaInvalido();
            return;
        }
        if (numeroProducao < 1 || numeroProducao > 999) {
            retornaInvalido();
            return;
        }
            //Se o numero for valido, puxa os dados do cliente e o historico de atendimento e preenche na tela
            //Se não, devolve uma mensagem de equipamento não encontrado
            try (Connection conexao = Conexao.conectar()) {
                EquipamentoRepository eRepo = new EquipamentoRepository();
                equipamentoAtual = eRepo.buscaNumeroSerie(conexao, numeroSerie);

                if (equipamentoAtual != null) {
                    Cliente cliente = equipamentoAtual.getCliente();

                    lblResultadoBusca.getStyleClass().add("mensagemSucesso");

                    lblResultadoBusca.setText("✔ Equipamento encontrado");
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
                    for (Atendimento atendimento : historico){
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
                    lblResultadoBusca.setText("✖ Equipamento não encontrado");
                    lblResultadoBusca.getStyleClass().add("mensagemErro");
                    }
            } catch (SQLException e) {
                lblResultadoBusca.setText("✖ Erro ao consultar o banco");
                lblResultadoBusca.getStyleClass().add("mensagemErro");
                }
    }


    private void limpaTela(){
        txtNumeroSerie.clear();
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
    @FXML
    private void salvarAtendimento() { }

    @FXML
    private void cancelar() {
        limpaTela();
    }
}
