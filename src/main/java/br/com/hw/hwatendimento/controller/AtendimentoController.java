package br.com.hw.hwatendimento.controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import br.com.hw.hwatendimento.repositories.*;
import br.com.hw.hwatendimento.model.*;
import javafx.fxml.FXML;

import java.time.LocalDate;

public class AtendimentoController {
    @FXML private TextField txtNumeroSerie, txtEmpresa;
    @FXML private Label lblResultadoBusca;
    @FXML private ComboBox<String> cmbModelo;
    @FXML private ToggleGroup tipoCliente;
    @FXML private RadioButton rbJuridica;

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
            txtEmpresa.setDisable(!rbJuridica.isSelected());
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

            //Verificaçao se o numero de serie é valido
            if(numeroSerie.length() == 11){
                if (numeroSerie.substring(2).matches("\\d{9}")){
                    String modelo = numeroSerie.substring(0, 2);
                    String versao = numeroSerie.substring(2, 4);
                    int ano = Integer.parseInt(numeroSerie.substring(4, 6));
                    int anoAtual = LocalDate.now().getYear() % 100;
                    int mes = Integer.parseInt(numeroSerie.substring(6,8));
                    int numeroProducao = Integer.parseInt(numeroSerie.substring(8,11));

                    if(modelo.equals("TC") || modelo.equals("TE") || modelo.equals("EC") || modelo.equals("TP")) {
                        if(versao.equals("10") || (versao.equals("11") && modelo.equals("EC"))){
                            if(ano >= 9 && ano <= anoAtual){
                                if(mes >= 1 && mes <= 12){
                                    if (numeroProducao >= 1 && numeroProducao <= 999){
                                        //Verificaçao se o numero de serie existe no banco (Depois de ver se é valido)
                                        EquipamentoRepository eRepo = new EquipamentoRepository();
                                        Equipamento equipamento = eRepo.buscaNumeroSerie(Conexao.conectar(), numeroSerie);
                                        if (equipamento != null){
                                            lblResultadoBusca.setText("✔ Equipamento encontrado");
                                            lblResultadoBusca.getStyleClass().add("mensagemSucesso");
                                        } else{
                                            lblResultadoBusca.setText("✖ Equipamento não encontrado");
                                            lblResultadoBusca.getStyleClass().add("mensagemErro");
                                        }
                                    } else{retornaInvalido();}
                                } else{retornaInvalido();}
                            } else{retornaInvalido();}
                        } else{retornaInvalido();}
                    } else{retornaInvalido();}
                } else{retornaInvalido();}
            } else{retornaInvalido();}
    }
    @FXML
    private void salvarAtendimento() { }

    @FXML
    private void cancelar() { }
}
