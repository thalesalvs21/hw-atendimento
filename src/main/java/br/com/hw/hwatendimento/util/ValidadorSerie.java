package br.com.hw.hwatendimento.util;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class ValidadorSerie {


    public static boolean validar(String campo){

        if (!campo.matches("[A-Z]{2}\\d{9}")) {
            return false;
        }

        String modelo = campo.substring(0, 2);
        String versao = campo.substring(2, 4);
        int ano = Integer.parseInt(campo.substring(4, 6));
        int anoAtual = LocalDate.now().getYear() % 100;
        int mes = Integer.parseInt(campo.substring(6, 8));
        int numeroProducao = Integer.parseInt(campo.substring(8, 11));

        //Valida se o numero de serie é valido
        if (!modelo.equals("TC") && !modelo.equals("TE") && !modelo.equals("EC") && !modelo.equals("TP")) {
            return false;
        }
        if (!versao.equals("10") && !(versao.equals("11") && modelo.equals("EC"))) {
            return false;
        }
        if (ano < 9 || ano > anoAtual) {
            return false;
        }
        if (mes < 1 || mes > 12) {
            return false;
        }
        if (numeroProducao < 1 || numeroProducao > 999) {
            return false;
        }
        return true;
    }
}
