package br.com.hw.hwatendimento.util;

import br.com.hw.hwatendimento.model.Usuario;

// apenas guarda quem esta logado
public class Sessao {

    private static Usuario usuarioLogado;

    public static void entrar(Usuario usuario) {
        usuarioLogado = usuario;
    }

    public static void sair() {
        usuarioLogado = null;
    }

    public static Usuario getUsuario() {
        return usuarioLogado;
    }

    public static boolean isAdmin() {
        return usuarioLogado != null && usuarioLogado.isAdmin();
    }
}
