package com.automacao.cadastrador_usuarios.domain;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class VinculoSuggestion {

    public List<String> sugerir(String perfil, List<String> obrasDaFilial) {
        if (perfil == null) return List.of();

        String p = perfil.toLowerCase(Locale.ROOT);

        if (p.contains("gerencial")) return obrasDaFilial;

        if (p.contains("civil")) {
            return obrasDaFilial.stream()
                    .filter(o -> !isInstall(o))
                    .filter(o -> !isEpi(o))
                    .collect(Collectors.toList());
        }

        if (p.contains("install")) {
            return obrasDaFilial.stream().filter(this::isInstall).collect(Collectors.toList());
        }
        return List.of();
    }

    private boolean isInstall(String obra) {
        return obra != null && obra.toUpperCase().contains("INSTALL");
    }

    private boolean isEpi(String obra) {
        if (obra == null) return false;
        String u = obra.toUpperCase();
        return u.contains("EPI") || u.contains("EPI'S") || u.contains("EPI’S");
    }
}
