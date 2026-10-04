package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Atendimento;
import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.repositories.AtendimentoRepository;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.util.Mascaras;
import br.com.hw.hwatendimento.util.Navegacao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PesquisaController {
    @FXML private DatePicker dtDe, dtAte;
    @FXML private TextField txtNomeFiltro, txtSerieFiltro, txtDescricaoFiltro, txtTelefoneFiltro;
    @FXML private Label lblStatusPesquisa, lblTotal;
    @FXML private TableView<Atendimento> tblResultados;
    @FXML private TableColumn<Atendimento, String> colData, colModelo, colSerie, colCliente, colTelefone, colDuracao, colDescricao;

    @FXML
    private void initialize() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Ensina a tabela o que mostrar em cada coluna: o que esta entre
        // parenteses e executado uma vez por linha, e "dado.getValue()"
        // e o Atendimento daquela linha.
        colData.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getDataHoraInicio().format(formato)));

        colModelo.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getEquipamento().getModelo()));

        colSerie.setCellValueFactory(dado -> {
            String serie = dado.getValue().getEquipamento().getNumeroSerie();
            return new SimpleStringProperty(serie == null ? "—" : serie);
        });

        colCliente.setCellValueFactory(dado -> {
            Cliente c = dado.getValue().getCliente();
            String texto = "J".equals(c.getTipo()) ? c.getNomeEmpresa() : c.getNome();
            return new SimpleStringProperty(texto);
        });

        colTelefone.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getCliente().getTelefone()));

        colDuracao.setCellValueFactory(dado ->
                new SimpleStringProperty(calcularDuracao(dado.getValue())));

        colDescricao.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getDescricao()));

        Mascaras.telefone(txtTelefoneFiltro);
        Mascaras.serie(txtSerieFiltro);
    }

    private String calcularDuracao(Atendimento atendimento) {
        if (atendimento.getDataHoraFim() == null) {
            return "em aberto";
        }
        Duration d = Duration.between(atendimento.getDataHoraInicio(), atendimento.getDataHoraFim());
        return d.toHours() + "h " + d.toMinutesPart() + "min";
    }

    @FXML
    private void pesquisar() {
        try (Connection conexao = Conexao.conectar()) {
            AtendimentoRepository repo = new AtendimentoRepository();
            List<Atendimento> resultados = repo.pesquisar(
                    conexao,
                    dtDe.getValue(),
                    dtAte.getValue(),
                    txtNomeFiltro.getText(),
                    txtSerieFiltro.getText(),
                    txtDescricaoFiltro.getText(),
                    txtTelefoneFiltro.getText()
            );

            tblResultados.setItems(FXCollections.observableArrayList(resultados));
            lblTotal.setText(resultados.size() + " atendimento(s)");
            lblStatusPesquisa.setText("");

        } catch (SQLException e) {
            lblStatusPesquisa.setText("✖ Erro ao consultar o banco");
        }
    }

    @FXML
    private void limparFiltros() {
        dtDe.setValue(null);
        dtAte.setValue(null);
        txtNomeFiltro.clear();
        txtSerieFiltro.clear();
        txtDescricaoFiltro.clear();
        txtTelefoneFiltro.clear();
        tblResultados.getItems().clear();
        lblTotal.setText("");
    }

    @FXML
    private void voltar(){
        Navegacao.trocarTela(txtNomeFiltro, "atendimento-view.fxml");
    }
}
