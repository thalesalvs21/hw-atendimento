package br.com.hw.hwatendimento.repositories;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.model.Equipamento;
public class EquipamentoRepository {

    public Equipamento buscaNumeroSerie(Connection conexao, String numeroSerie) {
        String sql = "Select * from equipamento where numero_serie = ?";
        Equipamento equipamento = null;

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, numeroSerie);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    equipamento = new Equipamento();
                    equipamento.setId(rs.getInt("id"));
                    equipamento.setModelo(rs.getString("modelo"));
                    equipamento.setNumeroSerie(rs.getString("numero_serie"));
                        int clienteId = rs.getInt("cliente_id");
                        ClienteRepository clienteRepo = new ClienteRepository();
                    equipamento.setCliente(clienteRepo.buscaId(conexao, clienteId));
                    equipamento.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar equipamento: " + e.getMessage());
        }
        return equipamento;

    }

    public Equipamento inserirEquipamento(Connection conexao, Equipamento equipamento) {
        String sql = "insert into equipamento (modelo, numero_serie, cliente_id) values (?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS -> serve para pedir o id para o banco
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, equipamento.getModelo());
            stmt.setString(2, equipamento.getNumeroSerie());
            stmt.setInt(3, equipamento.getCliente().getId());
            stmt.executeUpdate();

            // Recolhe o valor do id que ja foi pedido
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    equipamento.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir equipamento: " + e.getMessage());
        }
        return equipamento;
    }


    public static void main(String[] args){
        //Cria o cliente
        Cliente cliente = new Cliente();
        cliente.setNome("Teste");
        cliente.setTipo("F");
        cliente.setTelefone("31999995555");
        cliente.setNomeEmpresa(null);

        ClienteRepository cRepo = new ClienteRepository();
        cRepo.inserirCliente(Conexao.conectar(), cliente);
        //Cria o equipamento
        Equipamento equipamento = new Equipamento();
        equipamento.setModelo("ECGV6");
        equipamento.setNumeroSerie("EC090909099");
        // liga o equipamento ao cliente criado acima
        equipamento.setCliente(cliente);

        EquipamentoRepository repo = new EquipamentoRepository();
        repo.inserirEquipamento(Conexao.conectar(), equipamento);

        System.out.println(equipamento.getId());

        /* Testando busca por numero de serie
        EquipamentoRepository repo = new EquipamentoRepository();
        Equipamento equipamento = repo.buscaNumeroSerie(Conexao.conectar(), "XX999");

        if (equipamento != null) {
            System.out.println("Nome do cliente: " + equipamento.getCliente().getNome());
            System.out.println(equipamento);
            System.out.println(equipamento.getNumeroSerie());
            System.out.println(equipamento.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } else {
            System.out.println("Equipamento não encontrado");
        } */
    }
}
