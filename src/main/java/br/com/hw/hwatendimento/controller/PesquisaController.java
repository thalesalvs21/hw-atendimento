package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Atendimento;
import br.com.hw.hwatendimento.util.Navegacao;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PesquisaController {

    @FXML private DatePicker dtDe, dtAte;
    @FXML private TextField txtNomeFiltro, txtSerieFiltro, txtDescricaoFiltro, txtTelefoneFiltro;
    @FXML private Label lblStatusPesquisa, lblTotal;
    @FXML private TableView<Atendimento> tblResultados;
    @FXML private TableColumn<Atendimento, String> colData, colModelo, colSerie, colCliente, colTelefone, colDuracao, colDescricao;

    @FXML
    private void pesquisar(){

    }

    @FXML
    private void limparFiltros(){

    }

    @FXML
    private void voltar(){
        Navegacao.trocarTela(txtNomeFiltro, "atendimento-view.fxml");
    }
}
