package br.com.hw.hwatendimento.repositories;

import br.com.hw.hwatendimento.model.Cliente;
import br.com.hw.hwatendimento.model.Usuario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    // lista apenas os usuarios ativos
    public List<Usuario> listarAtivos(Connection conexao) throws SQLException {
        String sql = "select * from usuario where ativo = true order by nome";
        List<Usuario> lista = new ArrayList<>();

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }
    //lista todos os usuarios
    public List<Usuario> listarTodos(Connection conexao) throws SQLException{
        String sql = "select * from usuario order by nome";
        List<Usuario> lista = new ArrayList<>();

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(montar(rs));
            }
        }
        return lista;
    }

    // confere se a senha do usuario esta certa, se sim ele loga, se nao retorna null
    public Usuario autenticar(Connection conexao, int usuarioId, String senha) throws SQLException {
        String sql = "select * from usuario where id = ? and senha = ? and ativo = true";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, senha);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montar(rs);
                }
            }
        }
        return null;
    }

    // transforma uma linha do banco em um Usuario, os dois metodos acima usam
    private Usuario montar(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setAdmin(rs.getBoolean("admin"));
        usuario.setAtivo(rs.getBoolean("ativo"));
        usuario.setCriadoEm(rs.getObject("criado_em", LocalDateTime.class));
        return usuario;
    }

    // insere o usuario no banco
    public Usuario inserirUsuario(Connection conexao, Usuario usuario) throws SQLException {
        String sql = "insert into usuario (nome, senha, admin) values (?, ?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getSenha());
            stmt.setBoolean(3, usuario.isAdmin());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }
        }
        return usuario;
    }

    // altera o estado do usuario, ativo ou nao ativo
    public void alterarAtivo(Connection conexao, int usuarioId, boolean ativo) throws SQLException {
        String sql = "update usuario set ativo = ? where id = ?";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setBoolean(1, ativo);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();
        }
    }

}
