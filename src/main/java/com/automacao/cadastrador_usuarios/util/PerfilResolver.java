package com.automacao.cadastrador_usuarios.util;

public class PerfilResolver {

    public static String resolver(String funcao, boolean terceirizado) {
        if (funcao == null || funcao.isBlank()) return null;

        String cargo = funcao.trim();

        if (cargo.equalsIgnoreCase("Encarregado")) {
            return terceirizado ? "Operacional" : "Equipe de Apoio - CIVIL";
        }

        return null;
    }
}