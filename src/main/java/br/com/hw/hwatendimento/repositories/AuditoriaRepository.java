package br.com.hw.hwatendimento.repositories;

import br.com.hw.hwatendimento.model.Auditoria;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaRepository {

    // grava um evento no log, atendimentoId e detalhe podem vir null (ex: no loginN)
    public void registrar(Connection conexao, int usuarioId, String acao, Integer atendimentoId, String detalhe) throws SQLException {
        String sql = "insert into auditoria (usuario_id, acao, atendimento_id, detalhe) values (?, ?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, acao);

            if (atendimentoId == null) {
                stmt.setNull(3, Types.INTEGER);
            } else {
                stmt.setInt(3, atendimentoId);
            }

            stmt.setString(4, detalhe);
            stmt.executeUpdate();
        }
    }

    // os registros mais recentes primeiro, ja com o nome do usuario
    public List<Auditoria> listar(Connection conexao) throws SQLException {
        String sql = "select a.*, u.nome as usuario_nome " +
                "from auditoria a " +
                "join usuario u on u.id = a.usuario_id " +
                "order by a.criado_em desc " +
                "limit 500";
        List<Auditoria> lista = new ArrayList<>();

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Auditoria registro = new Auditoria();
                registro.setId(rs.getInt("id"));
                registro.setUsuarioId(rs.getInt("usuario_id"));
                registro.setUsuarioNome(rs.getString("usuario_nome"));
                registro.setAcao(rs.getString("acao"));
                registro.setAtendimentoId(rs.getObject("atendimento_id", Integer.class));
                registro.setDetalhe(rs.getString("detalhe"));
                registro.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
                lista.add(registro);
            }
        }
        return lista;
    }
}