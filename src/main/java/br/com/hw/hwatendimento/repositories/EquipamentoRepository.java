package br.com.hw.hwatendimento.repositories;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static void main(String[] args){
        EquipamentoRepository repo = new EquipamentoRepository();
        Equipamento equipamento = repo.buscaNumeroSerie(Conexao.conectar(), "XX999");

        if (equipamento != null) {
            System.out.println("Nome do cliente: " + equipamento.getCliente().getNome());
            System.out.println(equipamento);
            System.out.println(equipamento.getNumeroSerie());
            System.out.println(equipamento.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } else {
            System.out.println("Equipamento não encontrado");
        }
    }
}
