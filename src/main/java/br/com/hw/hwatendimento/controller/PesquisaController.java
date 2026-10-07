package br.com.hw.hwatendimento.controller;

import br.com.hw.hwatendimento.model.Atendimento;
import br.com.hw.hwatendimento.controller.AtendimentoController;
import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.repositories.AtendimentoRepository;
import br.com.hw.hwatendimento.repositories.Conexao;
import br.com.hw.hwatendimento.util.Mascaras;
import br.com.hw.hwatendimento.util.Navegacao;
import br.com.hw.hwatendimento.util.Sessao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PesquisaController {
    @FXML private DatePicker dtDe, dtAte;
    @FXML private TextField txtNomeFiltro, txtSerieFiltro, txtDescricaoFiltro, txtTelefoneFiltro;
    @FXML private Label lblStatusPesquisa, lblTotal;
    @FXML private TableView<Atendimento> tblResultados;
    @FXML private TableColumn<Atendimento, String> colData, colModelo, colSerie, colCliente, colTelefone, colDuracao, colDescricao;
    @FXML private Button btnPesquisar;
    @FXML private Button btnIrUsuarios;

    @FXML
    private void initialize() {
        btnIrUsuarios.setVisible(Sessao.isAdmin());
        btnIrUsuarios.setManaged(Sessao.isAdmin());
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

    private void mensagem(String mensagem, String classe){
        lblStatusPesquisa.getStyleClass().removeAll("mensagemSucesso", "mensagemErro", "mensagemCarregando");
        lblStatusPesquisa.setText(mensagem);
        lblStatusPesquisa.getStyleClass().add(classe);
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
        mensagem("Buscando....", "mensagemCarregando");
        btnPesquisar.setDisable(true);

        // guarda os valores dos campos antes, porque la dentro nao se pode mexer na tela.
        LocalDate de = dtDe.getValue();
        LocalDate ate = dtAte.getValue();
        String nome = txtNomeFiltro.getText();
        String serie = txtSerieFiltro.getText();
        String descricao = txtDescricaoFiltro.getText();
        String telefone = txtTelefoneFiltro.getText();

        Task<List<Atendimento>> tarefa = new Task<>() {
            @Override
            protected List<Atendimento> call() throws Exception {
                // isso roda em paralelo, sem travar a tela (tenta fazer a conexão sem que o programa crashe)
                try (Connection conexao = Conexao.conectar()) {
                    AtendimentoRepository repo = new AtendimentoRepository();
                    return repo.pesquisar(conexao, de, ate, nome, serie, descricao, telefone);
                }
            }
        };

        // chamado quando a busca termina bem
        tarefa.setOnSucceeded(e -> {
            List<Atendimento> resultados = tarefa.getValue();
            tblResultados.setItems(FXCollections.observableArrayList(resultados));
            lblTotal.setText(resultados.size() + " atendimento(s)");
            mensagem("Busca dos registros concluida!", "mensagemSucesso");
            btnPesquisar.setDisable(false);
        });

        // chamado quando da erro
        tarefa.setOnFailed(e -> {
            mensagem("✖ Erro ao consultar o banco", "mensagemErro");
            btnPesquisar.setDisable(false);
        });

        new Thread(tarefa).start();
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

    @FXML
    private void abrirUsuarios() {
        Navegacao.trocarTela(txtNomeFiltro, "usuario-view.fxml");
    }
}
