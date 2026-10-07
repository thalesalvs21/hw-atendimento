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

public class DetalheAtendimentoController {
    @FXML private Label lblStatus, lblMensagem;
    @FXML private TextField txtCliente, txtTelefone, txtSerie, txtModelo, txtInicio, txtHoraFim;
    @FXML private DatePicker dtFim;
    @FXML private TextArea txtDescricao;
    @FXML private Button btnFinalizar;

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

        mensagem("Salvando...", "mensagemCarregando");
        btnFinalizar.setDisable(true);

        Task<Boolean> tarefa = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try (Connection conexao = Conexao.conectar()) {
                    AtendimentoRepository repo = new AtendimentoRepository();
                    return repo.finalizarAtendimento(conexao, dados);
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
}