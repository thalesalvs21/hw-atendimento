package br.com.hw.hwatendimento.repositories;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Conexao {


    private static final Properties config = carregarConfig();

    private static Properties carregarConfig() {
        Properties p = new Properties();
        try (FileInputStream arquivo = new FileInputStream("config.properties")) {
            p.load(arquivo);
        } catch (IOException e) {
            System.err.println("Não foi possível ler o config.properties: " + e.getMessage());
        }
        return p;
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.usuario"),
                config.getProperty("db.senha")
        );
    }

}