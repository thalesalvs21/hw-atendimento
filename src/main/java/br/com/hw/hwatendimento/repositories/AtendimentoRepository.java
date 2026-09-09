package br.com.hw.hwatendimento.repositories;
import br.com.hw.hwatendimento.model.Atendimento;
import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.model.Equipamento;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class AtendimentoRepository {

    public Atendimento inserirAtendimento(Connection conexao, Atendimento atendimento) {
        String sql = "insert into atendimento (equipamento_id, cliente_id, data_hora_inicio, data_hora_fim, descricao) values (?, ?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS -> serve para pedir o id para o banco
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, atendimento.getEquipamento().getId());
            stmt.setInt(2, atendimento.getCliente().getId());
            stmt.setObject(3, atendimento.getDataHoraInicio());
            stmt.setObject(4, atendimento.getDataHoraFim());
            stmt.setString(5, atendimento.getDescricao());
            stmt.executeUpdate();

            // Recolhe o valor do id que ja foi pedido
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    atendimento.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir atendimento: " + e.getMessage());
        }
        return atendimento;
    }

    public List<Atendimento> buscaPorEquipamento(Connection conexao, int equipamentoId){
        String sql = "select * from atendimento where equipamento_id = ? order by data_hora_inicio desc";
        //Cria uma lista pra guardar obejetos apenas da classe atendimento
        List<Atendimento> lista = new ArrayList<>();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, equipamentoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Atendimento atendimento = new Atendimento();
                    atendimento.setId(rs.getInt("id"));
                    atendimento.setDataHoraInicio(rs.getObject("data_hora_inicio", LocalDateTime.class));
                    atendimento.setDataHoraFim(rs.getObject("data_hora_fim", LocalDateTime.class));
                    atendimento.setDescricao(rs.getString("descricao"));
                    lista.add(atendimento);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao localizar atendimentos existentes: " + e.getMessage());
        }
        return lista;
    }


    public static void main(String[] args){
        // Busca pelo numero de serie e pega o id do equipamento
        EquipamentoRepository eRepo = new EquipamentoRepository();
        Equipamento equipamento = eRepo.buscaNumeroSerie(Conexao.conectar(), "TE090909999");
        if (equipamento != null) {
            //Chama o metodo e guarda numa lista
            AtendimentoRepository repo = new AtendimentoRepository();
            List<Atendimento> lista = repo.buscaPorEquipamento(Conexao.conectar(), equipamento.getId());
            for (Atendimento atendimento : lista) {
                System.out.println(atendimento.getDataHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " - " + atendimento.getDescricao());
            }
        } else {
            System.out.println("Equipamento não encontrado");
        }
        /* Testa insert atendimento
        //Cria o cliente
        Cliente cliente = new Cliente();
        cliente.setNome("Teste3");
        cliente.setTipo("J");
        cliente.setNomeEmpresa("Teste Empresa");
        cliente.setTelefone("31999999949");

        ClienteRepository cRepo = new ClienteRepository();
        cRepo.inserirCliente(Conexao.conectar(), cliente);

        EquipamentoRepository eRepo = new EquipamentoRepository();
        Equipamento equipamento = eRepo.buscaNumeroSerie(Conexao.conectar(), "TE090909999");

        if (equipamento != null) {
            Atendimento atendimento = new Atendimento();
            atendimento.setEquipamento(equipamento);
            atendimento.setCliente(equipamento.getCliente());
            atendimento.setDataHoraInicio(LocalDateTime.now());
            atendimento.setDataHoraFim(null);
            atendimento.setDescricao("Segundo atendimento de teste");

            AtendimentoRepository repo = new AtendimentoRepository();
            repo.inserirAtendimento(Conexao.conectar(), atendimento);

            System.out.println("Atendimento criado: " + atendimento.getId());
        } else {
            System.out.println("Equipamento não encontrado");
        }

        //Cria o atendimento
        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        atendimento.setEquipamento(equipamento);
        atendimento.setDataHoraInicio(LocalDateTime.now());
        atendimento.setDataHoraFim(null);
        atendimento.setDescricao("Rodando checatab");

        AtendimentoRepository repo = new AtendimentoRepository();
        repo.inserirAtendimento(Conexao.conectar(), atendimento);

        System.out.println(atendimento.getId());
        */
    }
}