package br.com.hw.hwatendimento.controller;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import br.com.hw.hwatendimento.repositories.*;
import br.com.hw.hwatendimento.model.*;
import javafx.fxml.FXML;

public class AtendimentoController {
    @FXML private TextField txtNumeroSerie;
    @FXML private Label lblResultadoBusca;

    @FXML
    private void buscarEquipamento() {
        String numeroSerie = txtNumeroSerie.getText();
        EquipamentoRepository eRepo = new EquipamentoRepository();
        Equipamento equipamento = eRepo.buscaNumeroSerie(Conexao.conectar(), numeroSerie);
        if (equipamento != null){
            lblResultadoBusca.setText("✔ Equipamento encontrado");
            lblResultadoBusca.getStyleClass().add("mensagemSucesso");
        }
        else{
            lblResultadoBusca.setText("✖ Equipamento não encontrado");
            lblResultadoBusca.getStyleClass().add("mensagemErro");
        }
    }

    @FXML
    private void salvarAtendimento() { }

    @FXML
    private void cancelar() { }
}
