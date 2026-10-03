package br.com.hw.hwatendimento;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HWApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HWApplication.class.getResource("atendimento-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("HW Atendimento");
        stage.getIcons().addAll(
                new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-16.png")),
                new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-32.png")),
                new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-48.png")),
                new Image(getClass().getResourceAsStream("/br/com/hw/hwatendimento/assets/icone-256.png"))
        );
        stage.setScene(scene);
        stage.show();
    }
}
