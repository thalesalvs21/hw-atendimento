module br.com.hw.hwatendimento {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens br.com.hw.hwatendimento to javafx.fxml;
    opens br.com.hw.hwatendimento.controller to javafx.fxml;
    exports br.com.hw.hwatendimento;
}