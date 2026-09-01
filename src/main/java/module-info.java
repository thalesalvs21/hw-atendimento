module br.com.hw.hwatendimento {
    requires javafx.controls;
    requires javafx.fxml;


    opens br.com.hw.hwatendimento to javafx.fxml;
    opens br.com.hw.hwatendimento.controller to javafx.fxml;
    exports br.com.hw.hwatendimento;
}