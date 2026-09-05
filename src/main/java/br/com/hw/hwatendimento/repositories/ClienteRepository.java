package br.com.hw.hwatendimento.repositories;
import br.com.hw.hwatendimento.model.Cliente;

import java.sql.*;
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

    public Cliente inserirValores(Connection conexao, Cliente cliente) {
        String sql = "insert into cliente (tipo, nome, nome_empresa, telefone) values (?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS -> serve para pedir o id para o banco
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getTipo());
            stmt.setString(2, cliente.getNome());
            stmt.setString(3, cliente.getNomeEmpresa());
            stmt.setString(4, cliente.getTelefone());
            stmt.executeUpdate();

            // Recolhe o valor do id que ja foi pedido
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir cliente: " + e.getMessage());
        }
        return cliente;
    }


    public static void main(String[] args){
        // cria o cliente primeiro e depois roda o insert
        Cliente cliente = new Cliente();
        cliente.setNome("Teste");
        cliente.setTipo("F");
        cliente.setTelefone("31999995555");
        cliente.setNomeEmpresa(null);

        ClienteRepository repo = new ClienteRepository();
        repo.inserirValores(Conexao.conectar(), cliente);

        System.out.println(cliente.getId());

        /* teste select
        //puxa o cliente pelo id e depois printa os dados no console
        ClienteRepository repo = new ClienteRepository();
        Cliente cliente = repo.buscaId(Conexao.conectar(), 1);

        System.out.println(repo.buscaId(Conexao.conectar(), 1));
        System.out.println(cliente.getId());
        System.out.println(cliente.getTipo());
        System.out.println(cliente.getTelefone());
        System.out.println(cliente.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    */

    }
}
