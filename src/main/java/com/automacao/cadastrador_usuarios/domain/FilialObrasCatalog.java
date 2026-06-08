package com.automacao.cadastrador_usuarios.domain;

import java.util.*;

public class FilialObrasCatalog {

    private final Map<BaseState, List<Filial>> catalog = new EnumMap<>(BaseState.class);

    public FilialObrasCatalog() {
        catalog.put(BaseState.SP, buildSp());
        catalog.put(BaseState.RJ, buildRj());
    }

    public List<Filial> filiais(BaseState base) {
        return catalog.getOrDefault(base, List.of());
    }

    public Optional<Filial> findFilialByCode(BaseState base, String filialCode) {
        if (filialCode == null) return Optional.empty();
        return filiais(base).stream().filter(f -> f.code.equalsIgnoreCase(filialCode.trim())).findFirst();
    }

    public record Filial(String code, String nome, List<String> obras) {}

    private List<Filial> buildSp() {
        return List.of(
                new Filial("CCISA69", "KINEA", List.of(
                        "DEZ JARDIM","DEZ JARDIM - EPI’S","DEZ JARDIM - INSTALL","MÁXIMO JARDIM","MÁXIMO JARDIM - INSTALL","TUTTO JARDIM","TUTTO JARDIM - INSTALL"
                )),
                new Filial("CCISA118", "SQUARE PANAMBY", List.of(
                        "GIOVANNI GRONCHI","GIOVANNI GRONCHI - EPI’S","GIOVANNI GRONCHI - INSTALL","ITAPAIUNA","ITAPAIUNA - INSTALL","UNIQ","UNIQ - INSTALL"
                )),
                new Filial("CCISA108", "ENERGY GUARULHOS", List.of(
                        "ENERGY GUARULHOS","ENERGY GUARULHOS - EPI’S","ENERGY GUARULHOS - INSTALL"
                )),
                new Filial("CCISA48", "MIGUEL YUNES", List.of(
                        "EXCLUSIVE MIGUEL YUNES","EXCLUSIVE MIGUEL YUNES - INSTALL","LIBERTY MIGUEL YUNES","LIBERTY MIGUEL YUNES - INSTALL",
                        "MIGUEL YUNES PARK","MIGUEL YUNES PARK - INSTALL","MY MIGUEL YUNES","MY MIGUEL YUNES - INSTALL","SOUL MIGUEL YUNES","SOUL MIGUEL YUNES - INSTALL"
                )),
                new Filial("CCISA107", "ELO SANTO ANDRÉ", List.of(
                        "ELO SANTO ANDRÉ","ELO SANTO ANDRÉ - EPI’S","ELO SANTO ANDRÉ - INSTALL"
                )),
                new Filial("CCISA66", "CIDADE MOOCA", List.of(
                        "CIDADE MOOCA - EPI'S","DUOMO","DUOMO - INSTALL","NAVONA","NAVONA - INSTALL","SAN MARCO","SAN MARCO - INSTALL",
                        "SAN PIETRO","SAN PIETRO - INSTALL","VENEZIA","VENEZIA - INSTALL"
                )),
                new Filial("CCISA80", "GUIDO PARQUE", List.of(
                        "GUIDO PARQUE - EPI’S","GUIDO PQ. NAÇÕES UNIDAS","GUIDO PQ. NAÇÕES UNIDAS - INSTALL","GUIDO PQ. SANTO AMARO","GUIDO PQ. SANTO AMARO - INSTALL"
                )),
                new Filial("MONTEREY", "GUEDALA PARK", List.of(
                        "COMPLEXO GUEDALA","GUEDALA PARK - EPI’S","GUEDALA PARK I - INSTALL","GUEDALA PARK II","GUEDALA PARK II - INSTALL",
                        "GUEDALA PARK III","GUEDALA PARK III - INSTALL"
                )),
                new Filial("CCISA117", "CIDADE CENTRAL", List.of(
                        "CIDADE CENTRAL","CIDADE CENTRAL - EPI’S","CIDADE CENTRAL - INSTALL"
                )),
                new Filial("CCISA77", "CIDADE JAGUARÉ", List.of(
                        "VILA ELDORADO","VILA ELDORADO - INSTALL","VILA LOBOS","VILA LOBOS - INSTALL"
                )),
                new Filial("CCISA122", "ARQUE REPUBLICA", List.of(
                        "ARQUE - EPI'S","ARQUE REPUBLICA","ARQUE REPUBLICA - INSTALL"
                )),
                new Filial("CCISA73", "URBAN VILA MARIA", List.of(
                        "URBAN VILA MARIA","URBAN VILA MARIA - EPI’S","URBAN VILA MARIA - INSTALL","URBAN VILA MARIA 2","URBAN VILA MARIA 2 - INSTALL"
                )),
                new Filial("CCISA110", "DEZ LIMÃO", List.of(
                        "DEZ LIMÃO","DEZ LIMÃO - EPI'S","DEZ LIMÃO - INSTALL"
                )),
                new Filial("CCISA160", "THE PLACE", List.of(
                        "THE PLACE - EPI'S","THE PLACE BARRA FUNDA","THE PLACE BARRA FUNDA - INSTALL"
                )),
                new Filial("CCISA106", "JOÃO DIAS", List.of(
                        "MÉRITO JOÃO DIAS","MÉRITO JOÃO DIAS - INSTALL","MÉRITO SANTO AMARO","MÉRITO SANTO AMARO - INSTALL"
                )),
                new Filial("CASAVIVA", "LIVECAMPOLIMPO", List.of(
                        "LIVE CAMPO LIMPO","LIVE CAMPO LIMPO - EPI'S","LIVE CAMPO LIMPO - INSTALL"
                )),
                new Filial("CCISA43", "ALTOS SÃO DOMINGOS", List.of(
                        "ALTO SÃO DOMINGOS","ALTO SÃO DOMINGOS - INSTALL"
                )),
                new Filial("CCISA34", "MÉRITO BARRA FUNDA", List.of(
                        "MÉRITO BARRA FUNDA","MÉRITO BARRA FUNDA - INSTALL"
                )),
                new Filial("CCISA177", "CIDADE VILA LOBOS", List.of(
                        "MAESTRO","MAESTRO - INSTALL","SONATA","SONATA - INSTALL","SOPRANO","SOPRANO - INSTALL"
                )),
                new Filial("CCISA29", "CIDADE LAPA", List.of(
                        "POMPÉIA","POMPÉIA - INSTALL","SANTA MARINA","SANTA MARINA - INSTALL","ÁGUA BRANCA","ÁGUA BRANCA - INSTALL"
                )),
                new Filial("CCISA95", "MÉRITO VILA MASCOTE", List.of(
                        "MÁXIMO VILA MASCOTE","MÁXIMO VILA MASCOTE - INSTALL","MÉRITO VILA MASCOTE","MÉRITO VILA MASCOTE - INSTALL"
                )),
                new Filial("CCISA37", "BELENZINHO", List.of(
                        "DEZ BELENZINHO","DEZ BELENZINHO - INSTALL","MÉRITO BELENZINHO","MÉRITO BELENZINHO - INSTALL"
                )),
                new Filial("CCISA86", "DEZ BUTANTÃ", List.of(
                        "DEZ BUTANTÃ","DEZ BUTANTÃ - INSTALL","SINGULAR BUTANTÃ","SINGULAR BUTANTÃ - INSTALL"
                )),
                new Filial("CCISA183", "SUPREME ANÁLIA FRANCO", List.of(
                        "SUPREME ANÁLIA FRANCO","SUPREME ANÁLIA FRANCO - INSTALL"
                )),
                new Filial("CCISA84", "360° PARK VIEW", List.of(
                        "360º PARK VIEW","360º PARK VIEW - INSTALL"
                )),
                new Filial("CCISA33", "MÉRITO GUARULHOS", List.of(
                        "MÉRITO GUARULHOS","MÉRITO GUARULHOS - INSTALL","NEXT GUARULHOS","NEXT GUARULHOS - INSTALL"
                )),
                new Filial("CCISA94", "ARTE SANTA MARINA", List.of(
                        "ARTE SANTA MARINA","ARTE SANTA MARINA - INSTALL"
                )),
                new Filial("CCISA124", "MODERN MOOCA", List.of(
                        "MODERN MOOCA","MODERN MOOCA - INSTALL"
                )),
                new Filial("CCISA172", "THE ONE", List.of(
                        "THE ONE CHÁCARA SANTO ANTONIO","THE ONE CHÁCARA SANTO ANTONIO - INSTALL","THE ONE GRANJA JULIETA","THE ONE GRANJA JULIETA - INSTALL"
                )),
                new Filial("CCISA92", "MY SACOMÃ", List.of(
                        "MY SACOMÃ","MY SACOMÃ - INSTALL"
                )),
                new Filial("CCISA96", "MIRAE BOM RETIRO", List.of(
                        "MIRAE BOM RETIRO","MIRAE BOM RETIRO - INSTALL"
                ))
        );
    }

    private List<Filial> buildRj() {
        return List.of(
                new Filial("CCISA97", "FLOW SANTA ROSA", List.of("FLOW SANTA ROSA","FLOW SANTA ROSA - INSTALL")),
                new Filial("CCISA67", "ORLA RECREIO", List.of("PRAIA DO PONTAL","PRAIA DO PONTAL - INSTALL","PRAINHA","PRAINHA - INSTALL","RESERVA","RESERVA - INSTALL")),
                new Filial("CCISA162", "TRENDY CACHAMBI", List.of("TRENDY CACHAMBI","TRENDY CACHAMBI - INSTALL")),
                new Filial("CCISA116", "PATEO NAZARETH", List.of("PATEO NAZARETH","PATEO NAZARETH - INSTALL")),
                new Filial("CCISA100", "MY JACAREPAGUA", List.of(
                        "MY JACAREPAGUÁ LIFE","MY JACAREPAGUÁ LIFE - INSTALL","MY JACAREPAGUÁ MOOD","MY JACAREPAGUÁ MOOD - INSTALL",
                        "MY JACAREPAGUÁ STYLE","MY JACAREPAGUÁ STYLE - INSTALL"
                )),
                new Filial("CCISA112", "RIO ENERGY", List.of("RIO ENERGY")),
                new Filial("CCISA140", "VARGAS 1140", List.of("VARGAS 1140","VARGAS 1140 - INSTALL")),
                new Filial("CCISA143", "EPICENTRO", List.of("EPICENTRO","EPICENTRO - INSTALL")),
                new Filial("CCISA90", "NOVA NORTE", List.of(
                        "NOVA NORTE GINGA","NOVA NORTE GINGA - INSTALL","NOVA NORTE RAIZES","NOVA NORTE RAIZES - Install","NOVA NORTE SAMBA","NOVA NORTE SAMBA - INSTALL"
                )),
                new Filial("CCISA141", "BAIA GUANABARA", List.of("BAIA GUANABARA - EPI’S","BAIA GUANABARA RESIDENCES","BAIA GUANABARA RESIDENCES - INSTALL")),
                new Filial("CCISA142", "RIO BRANCO 220", List.of("RIO BRANCO 220","RIO BRANCO 220 - INSTALL")),
                new Filial("CCISA113", "MIRANTE DA GUANABARA", List.of("MIRANTE DA GUANABARA","MIRANTE DA GUANABARA - INSTALL","MIRANTE GUANABARA - EPI’S")),
                new Filial("CCISA163", "PORTO MARAVILHA", List.of("PORTO MARAVILHA","PORTO MARAVILHA - EPI'S","PORTO MARAVILHA - INSTALL")),
                new Filial("CCISA145", "QUINTA DO BISPO", List.of("QUINTA DO BISPO","QUINTA DO BISPO - EPI'S","QUINTA DO BISPO - INSTALL")),
                new Filial("CCISA131", "HEITOR DOS PRAZERES", List.of("COLOMBINA","COLOMBINA - INSTALL","PIERROT","PIERROT - INSTALL")),
                new Filial("CCISA128", "AMERICAS 19", List.of("AMERICAS 19","AMERICAS 19 - INSTALL")),
                new Filial("CCISA20", "PARQUE BRITO 3", List.of("COMPLETO PARQUE BRITO 3","COMPLETO PARQUE BRITO 3 - INSTALL")),
                new Filial("CCISA28", "METROPOLITAN DREAM", List.of("METROPOLITAN DREAM","METROPOLITAN DREAM - INSTALL")),
                new Filial("CCISA135", "THE PIER", List.of("THE PIER","THE PIER - INSTALL")),
                new Filial("CCISA147", "CIATA", List.of("CIATA RESIDENCIAL","CIATA RESIDENCIAL - INSTALL")),
                new Filial("CCISA206", "ORLA MAUÁ", List.of("ORLA MAUÁ","ORLA MAUÁ - INSTALL")),
                new Filial("CCISA174", "NOVA OLARIA", List.of("NOVA OLARIA 2","NOVA OLARIA 2 - INSTALL","NOVA OLARIA I","NOVA OLARIA I - INSTALL")),
                new Filial("CCISA146", "ARCOS DO PORTO", List.of("ARCOS DO PORTO","ARCOS DO PORTO - INSTALL")),
                new Filial("CCISA101", "CAMINHOS DA GUANABARA", List.of("CAMINHOS DA GUANABARA","CAMINHOS DA GUANABARA - INSTALL"))
        );
    }
}
