package br.com.hw.hwatendimento.repositories;

import br.com.hw.hwatendimento.model.Anexo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnexoRepository {

    // recebe tambem o arquivo (conteudo), que vai direto para o banco
    public Anexo inserirAnexo(Connection conexao, Anexo anexo, byte[] conteudo) throws SQLException {
        String sql = "insert into anexo (atendimento_id, nome_arquivo, caminho, conteudo, usuario_id) values (?, ?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS -> serve para pedir o id para o banco
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, anexo.getAtendimentoId());
            stmt.setString(2, anexo.getNomeArquivo());
            stmt.setString(3, anexo.getCaminho());   // nos anexos novos fica vazio (null)
            stmt.setBytes(4, conteudo);
            stmt.setInt(5, anexo.getUsuarioId());
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

    // lista sem o conteudo, para nao trazer os arquivos inteiros so para mostrar os nomes
    public List<Anexo> listarPorAtendimento(Connection conexao, int atendimentoId) throws SQLException {
        String sql = "select id, atendimento_id, nome_arquivo, caminho, usuario_id, criado_em " +
                "from anexo where atendimento_id = ? order by criado_em";
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

    // busca so o arquivo de um anexo, na hora de abrir
    // devolve null se for um anexo antigo, que esta na pasta e nao no banco
    public byte[] buscarConteudo(Connection conexao, int anexoId) throws SQLException {
        String sql = "select conteudo from anexo where id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, anexoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("conteudo");
                }
            }
        }
        return null;
    }
}