package com.automacao.cadastrador_usuarios.infra;

import com.automacao.cadastrador_usuarios.domain.VinculoPlan;

import java.util.*;

public class VinculoGrouper {

    public Map<String, List<VinculoPlan>> groupByObrasKey(List<VinculoPlan> plans) {
        Map<String, List<VinculoPlan>> map = new LinkedHashMap<>();
        for (VinculoPlan p : plans) {
            String key = p.obrasAsKey();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }
        return map;
    }
}
