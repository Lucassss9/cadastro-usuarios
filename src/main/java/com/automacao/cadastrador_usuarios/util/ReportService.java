package com.automacao.cadastrador_usuarios.util;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportService {

    public String gerarRelatorioDetalhado(List<Map<String, String>> dados) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivo = "Relatorio_Cadastros_" + timestamp + ".csv";

        File destino = escolherPasta(nomeArquivo);

        try (FileWriter writer = new FileWriter(destino)) {
            writer.write('\ufeff');
            writer.write("DATA/HORA;NOME;EMAIL;CPF;FUNCAO;OBRAS;PERFIL;STATUS\n");

            for (Map<String, String> row : dados) {
                writer.write(limpar(safeGet(row, "data_hora")) + ";");
                writer.write(limpar(safeGet(row, "nome")) + ";");
                writer.write(limpar(safeGet(row, "email")) + ";");
                writer.write(limpar(safeGet(row, "cpf")) + ";");
                writer.write(limpar(safeGet(row, "funcao")) + ";");
                writer.write(limpar(safeGet(row, "obras_todas").isBlank()
                        ? safeGet(row, "obra") : safeGet(row, "obras_todas")) + ";");
                writer.write(limpar(safeGet(row, "perfil")) + ";");
                writer.write(limpar(safeGet(row, "status_cadastro")) + "\n");
            }
            return destino.getAbsolutePath();

        } catch (Exception e) {
            System.out.println("Erro ao gerar relatorio: " + e.getMessage());
            return "Nao consegui salvar o relatorio: " + e.getMessage();
        }
    }

    private File escolherPasta(String nomeArquivo) {
        String home = System.getProperty("user.home");

        String[] candidatas = {
                home + File.separator + "Desktop",
                home + File.separator + "\u00c1rea de Trabalho",
                home + File.separator + "OneDrive" + File.separator + "Desktop",
                home + File.separator + "OneDrive" + File.separator + "\u00c1rea de Trabalho",
                home
        };

        for (String caminho : candidatas) {
            File pasta = new File(caminho);
            if (pasta.exists() && pasta.isDirectory()) {
                return new File(pasta, nomeArquivo);
            }
        }

        return new File(home, nomeArquivo);
    }

    private String limpar(String texto) {
        if (texto == null) return "";
        return texto.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }

    private String safeGet(Map<String, String> map, String key) {
        String v = map.get(key);
        return v == null ? "" : v;
    }
}