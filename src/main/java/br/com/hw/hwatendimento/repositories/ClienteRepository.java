package br.com.hw.hwatendimento.repositories;
import br.com.hw.hwatendimento.model.Cliente;
import java.sql.Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClienteRepository {

    public Cliente buscaId(Connection conexao, int idBusca){
        String sql = "select * from cliente where id = ?";
        Cliente cliente = null;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, idBusca);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    cliente = new Cliente();
                    cliente.setId(rs.getInt("id"));
                    cliente.setTipo(rs.getString("tipo"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setNomeEmpresa(rs.getString("nome_empresa"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cliente: " + e.getMessage());
        }
        return cliente;
    }

    public static void main(String[] args){
        ClienteRepository repo = new ClienteRepository();
        Cliente cliente = repo.buscaId(Conexao.conectar(), 1);

        System.out.println(repo.buscaId(Conexao.conectar(), 1));
        System.out.println(cliente.getId());
        System.out.println(cliente.getTipo());
        System.out.println(cliente.getTelefone());
        System.out.println(cliente.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        // format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss") serve pra formatar a data e hora nesse modelo
    }
}
