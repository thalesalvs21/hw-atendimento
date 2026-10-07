package br.com.hw.hwatendimento.repositories;

import br.com.hw.hwatendimento.model.Anexo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnexoRepository {

    public Anexo inserirAnexo(Connection conexao, Anexo anexo) throws SQLException {
        String sql = "insert into anexo (atendimento_id, nome_arquivo, caminho, usuario_id) values (?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS -> serve para pedir o id para o banco
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, anexo.getAtendimentoId());
            stmt.setString(2, anexo.getNomeArquivo());
            stmt.setString(3, anexo.getCaminho());
            stmt.setInt(4, anexo.getUsuarioId());
            stmt.executeUpdate();

            // Recolhe o valor do id que ja foi pedido
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    anexo.setId(rs.getInt(1));
                }
            }
        }
        return anexo;
    }

    public List<Anexo> listarPorAtendimento(Connection conexao, int atendimentoId) throws SQLException {
        String sql = "select * from anexo where atendimento_id = ? order by criado_em";
        List<Anexo> lista = new ArrayList<>();

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, atendimentoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Anexo anexo = new Anexo();
                    anexo.setId(rs.getInt("id"));
                    anexo.setAtendimentoId(rs.getInt("atendimento_id"));
                    anexo.setNomeArquivo(rs.getString("nome_arquivo"));
                    anexo.setCaminho(rs.getString("caminho"));
                    anexo.setUsuarioId(rs.getInt("usuario_id"));
                    anexo.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
                    lista.add(anexo);
                }
            }
        }
        return lista;
    }
}