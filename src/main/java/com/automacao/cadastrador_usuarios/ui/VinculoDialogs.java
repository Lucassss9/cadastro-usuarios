package com.automacao.cadastrador_usuarios.ui;

import com.automacao.cadastrador_usuarios.domain.FilialObrasCatalog;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VinculoDialogs {

    public record VinculoPlan(String filialCode, List<String> obras) {}

    public VinculoPlan escolherFilialEObras(String nomeUsuario,
                                            List<FilialObrasCatalog.Filial> filiais,
                                            List<String> sugestaoMarcada) {

        if (filiais == null || filiais.isEmpty()) return null;

        JComboBox<String> comboFilial = new JComboBox<>();
        for (FilialObrasCatalog.Filial f : filiais) comboFilial.addItem(f.code() + " - " + f.nome());

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(18);

        Runnable carregarObrasDaFilialSelecionada = () -> {
            int idx = comboFilial.getSelectedIndex();
            if (idx < 0) return;

            FilialObrasCatalog.Filial filial = filiais.get(idx);

            model.clear();
            for (String o : filial.obras()) model.addElement(o);

            if (sugestaoMarcada != null && !sugestaoMarcada.isEmpty()) {
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < model.size(); i++) {
                    String v = model.get(i);
                    for (String s : sugestaoMarcada) {
                        if (v.equalsIgnoreCase(String.valueOf(s).trim())) {
                            indices.add(i);
                            break;
                        }
                    }
                }
                int[] arr = indices.stream().mapToInt(Integer::intValue).toArray();
                list.setSelectedIndices(arr);
            } else {
                list.clearSelection();
            }
        };

        carregarObrasDaFilialSelecionada.run();

        comboFilial.addActionListener(e -> carregarObrasDaFilialSelecionada.run());

        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(520, 300));

        Object[] form = {
                "Usuário:", new JLabel(Objects.toString(nomeUsuario, "")),
                "Filial:", comboFilial,
                "Selecione as obras:", scroll
        };

        int op = JOptionPane.showConfirmDialog(null, form, "Vínculo de Obras", JOptionPane.OK_CANCEL_OPTION);
        if (op != JOptionPane.OK_OPTION) return null;

        int idx = comboFilial.getSelectedIndex();
        if (idx < 0) return null;

        FilialObrasCatalog.Filial filial = filiais.get(idx);

        return new VinculoPlan(filial.code(), new ArrayList<>(list.getSelectedValuesList()));
    }
}
