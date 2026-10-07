package br.com.hw.hwatendimento.repositories;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexao {

    private static final Properties config = carregarConfig();

    private static Properties carregarConfig() {
        Properties p = new Properties();

        try {
            File origem = new File(Conexao.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File arquivo = new File(origem.getParentFile(), "config.properties");

            if (arquivo.exists()) {
                try (FileInputStream entrada = new FileInputStream(arquivo)) {
                    p.load(entrada);
                    return p;
                }
            }
        } catch (Exception e) {

        }

        try (FileInputStream entrada = new FileInputStream("config.properties")) {
            p.load(entrada);
        } catch (Exception e) {
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

    // devolve qualquer valor do config.properties (ex: "anexos.pasta")
    public static String getConfig(String chave) {
        return config.getProperty(chave);
    }
}