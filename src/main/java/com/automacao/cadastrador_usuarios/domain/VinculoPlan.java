package com.automacao.cadastrador_usuarios.domain;

import java.util.*;

public class VinculoPlan {
    private final Map<String, String> usuarioRow;
    private final List<String> obrasSelecionadas = new ArrayList<>();

    public VinculoPlan(Map<String, String> usuarioRow) {
        this.usuarioRow = Objects.requireNonNull(usuarioRow);
    }

    public Map<String, String> row() { return usuarioRow; }

    public String nome() { return usuarioRow.getOrDefault("nome", ""); }
    public String email() { return usuarioRow.getOrDefault("email", ""); }
    public String perfil() { return usuarioRow.getOrDefault("perfil", ""); }

    public List<String> obras() { return obrasSelecionadas; }

    public void setObras(List<String> obras) {
        obrasSelecionadas.clear();
        if (obras != null) obrasSelecionadas.addAll(obras);
    }

    public String obrasAsKey() {
        List<String> copy = new ArrayList<>(obrasSelecionadas);
        copy.replaceAll(s -> s == null ? "" : s.trim());
        copy.removeIf(String::isEmpty);
        copy.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join("|", copy);
    }

    public String obrasAsDisplay() {
        return String.join(", ", obrasSelecionadas);
    }
}
