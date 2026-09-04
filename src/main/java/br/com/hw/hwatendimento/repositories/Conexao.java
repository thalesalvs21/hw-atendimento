package br.com.hw.hwatendimento.repositories;
import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {
    private static final String Usuario = "root";
    private static final String Senha = "";
    private static final String URL = "jdbc:mysql://localhost:3306/hwatendimento";


    public static Connection conectar() {
        Connection conexao = null;
        try {
            conexao = DriverManager.getConnection(URL, Usuario, Senha);
        } catch (java.sql.SQLException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
        }
        return conexao;
    }
}