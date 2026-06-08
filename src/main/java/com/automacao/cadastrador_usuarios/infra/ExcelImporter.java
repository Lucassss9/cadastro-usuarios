package com.automacao.cadastrador_usuarios.infra;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ExcelImporter {

    public List<Map<String, String>> importarExcel() throws Exception {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione a Planilha");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel", "xlsx"));
        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection != JFileChooser.APPROVE_OPTION) return null;

        File file = fileChooser.getSelectedFile();
        List<Map<String, String>> lista = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nome = getCellValue(row.getCell(0));
                String email = getCellValue(row.getCell(1));
                String cpf = getCellValue(row.getCell(2));
                String funcao = getCellValue(row.getCell(3));
                String obra = getCellValue(row.getCell(4));
                String perfil = getCellValue(row.getCell(5));
                if (nome.isEmpty() && email.isEmpty()) continue;

                Map<String, String> dados = new HashMap<>();
                dados.put("nome", nome);
                dados.put("email", email);
                dados.put("cpf", cpf);
                dados.put("funcao", funcao);
                dados.put("obra", obra);
                dados.put("perfil", perfil);
                lista.add(dados);
            }
        }
        return lista;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                return String.valueOf((int) cell.getNumericCellValue());
            default: return "";
        }
    }
}
