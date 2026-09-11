package br.com.hw.hwatendimento.controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import br.com.hw.hwatendimento.repositories.*;
import br.com.hw.hwatendimento.model.*;
import javafx.fxml.FXML;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class AtendimentoController {
    @FXML private TextField txtNumeroSerie, txtEmpresa, txtNome, txtTelefone, txtHoraFim, txtHoraInicio;
    @FXML private TextArea txtDescricao;
    @FXML private Label lblResultadoBusca;
    @FXML private ComboBox<String> cmbModelo;
    @FXML private ToggleGroup tipoCliente;
    @FXML private RadioButton rbJuridica, rbFisica;
    @FXML private DatePicker dtInicio, dtFim;
    private Equipamento equipamentoAtual;

    @FXML
    private void initialize(){
        //Limita a caixa de texto em 11 caracteres
        txtNumeroSerie.setTextFormatter(new TextFormatter<String>(tamanho -> {
            if (tamanho.getControlNewText().length() <= 11) {
                return tamanho;
            } return null;
        }));
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
        String numeroSerie = txtNumeroSerie.getText().toUpperCase();

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
    }
    @FXML
    private void salvarAtendimento() { }

    @FXML
    private void cancelar() {
        limpaTela();
    }
}
