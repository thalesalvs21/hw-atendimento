package br.com.hw.hwatendimento.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class Mascaras {
    // Mascara horario
    public static void hora(TextField campo){
        campo.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (mudanca.isDeleted()) {
                return mudanca;
            }

            String texto = mudanca.getControlNewText();
            String digitos = texto.replaceAll("\\D", "");
            String formatado;


            if (digitos.length() > 4) {
                digitos = digitos.substring(0, 4);
            }

            if (digitos.length() <= 2) {
                formatado = digitos;
            } else {
                formatado = digitos.substring(0, 2) + ":" + digitos.substring(2);
            }
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.setCaretPosition(formatado.length());
            mudanca.setAnchor(formatado.length());

            return mudanca;
        }));
    }
    // Mascara telefone
    public static void telefone (TextField campo){
        campo.setTextFormatter(new TextFormatter<String>(mudanca -> {
            if (mudanca.isDeleted()) {
                return mudanca;
            }

            String texto = mudanca.getControlNewText();
            String digitos = texto.replaceAll("\\D", "");
            String formatado;


            if (digitos.length() > 11) {
                digitos = digitos.substring(0, 11);
            }

            if (digitos.length() <= 2) {
                formatado = digitos;
            } else if (digitos.length() <= 7) {
                formatado = "(" + digitos.substring(0, 2) + ") " + digitos.substring(2);
            } else {
                formatado = "(" + digitos.substring(0, 2) + ") "
                        + digitos.substring(2, 7) + "-"
                        + digitos.substring(7);
            }
            mudanca.setRange(0, mudanca.getControlText().length());
            mudanca.setText(formatado);
            mudanca.setCaretPosition(formatado.length());
            mudanca.setAnchor(formatado.length());
            return mudanca;
        }));
    }

    //Limita o numero de serie em 11 caracteres
    public static void serie(TextField campo){
        campo.setTextFormatter(new TextFormatter<String>(tamanho -> {
            if (tamanho.getControlNewText().length() <= 11) {
                return tamanho;
            } return null;
        }));
    }

}
