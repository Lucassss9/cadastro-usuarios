package com.automacao.cadastrador_usuarios.util;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportService {

    public String gerarRelatorioDetalhado(List<Map<String, String>> dados) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String path = System.getProperty("user.home") + "\\Desktop\\Relatorio_Cadastros_" + timestamp + ".csv";
            FileWriter writer = new FileWriter(path);
            writer.write('\ufeff');

            writer.write("DATA/HORA;NOME;EMAIL;CPF:FUNCAO;OBRA;PERFIL;STATUS_CADASTRO;OBRAS_VINCULO;STATUS_VINCULO\n");

            for (Map<String, String> row : dados) {
                writer.write(safeGet(row, "data_hora") + ";");
                writer.write(safeGet(row, "nome") + ";");
                writer.write(safeGet(row, "email") + ";");
                writer.write(safeGet(row, "cpf") + ";");
                writer.write(safeGet(row, "funcao") + ";");
                writer.write(safeGet(row, "obra") + ";");
                writer.write(safeGet(row, "perfil") + ";");
                writer.write(safeGet(row, "status_cadastro") + ";");
                writer.write(safeGet(row, "obras_vinculo") + ";");
                writer.write(safeGet(row, "status_vinculo") + "\n");
            }
            writer.close();
            return path;
        } catch (Exception e) {
            return "Erro ao gerar arquivo";
        }
    }

    private String safeGet(Map<String, String> map, String key) {
        return map.getOrDefault(key, "-");
    }
}
