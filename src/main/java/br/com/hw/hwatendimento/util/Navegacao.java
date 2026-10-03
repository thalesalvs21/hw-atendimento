package br.com.hw.hwatendimento.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import java.io.IOException;

public class Navegacao {

    public static void trocarTela(Node origem, String arquivo) {
        try {
            Parent tela = FXMLLoader.load(
                Navegacao.class.getResource("/br/com/hw/hwatendimento/" + arquivo)
            );
            origem.getScene().setRoot(tela);
        } catch (IOException e) {
            System.err.println("Erro ao abrir a tela: " + e.getMessage());
        }
    }
}