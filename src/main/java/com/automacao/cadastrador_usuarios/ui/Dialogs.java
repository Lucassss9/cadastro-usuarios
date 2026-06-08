package com.automacao.cadastrador_usuarios.ui;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Dialogs {

    public int escolherEstado() {
        Object[] opcoes = {"São Paulo (SP)", "Rio de Janeiro (RJ)"};
        return JOptionPane.showOptionDialog(null, "Qual estado da obra?", "Base",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
    }

    public int escolherModo() {
        Object[] botoesModo = {"Importar EXCEL", "Digitar MANUALMENTE"};
        return JOptionPane.showOptionDialog(null, "Como deseja inserir os dados?", "Modo de Operação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, botoesModo, botoesModo[0]);
    }

    public int escolherModoVinculo() {
        Object[] opcoes = {"🤖 Robô vincular", "✋ Vou vincular manualmente", "⏭️ Pular vínculo agora"};
        return JOptionPane.showOptionDialog(
                null,
                "Cadastros finalizados.\nComo você quer fazer o vínculo das obras?",
                "Vínculo de Obras",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );
    }

    public void info(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    public int confirmarSimNao(String msg, String titulo) {
        return JOptionPane.showConfirmDialog(null, msg, titulo, JOptionPane.YES_NO_OPTION);
    }

    public int validarCadastro(Map<String, String> dadosAtuais) {
        String resumo = "VERIFIQUE OS DADOS NA TELA:\n\n" +
                "Nome: " + dadosAtuais.get("nome") + "\n" +
                "Email: " + dadosAtuais.get("email") + "\n" +
                "Função: " + dadosAtuais.get("funcao") + "\n" +
                "CPF: " + dadosAtuais.get("cpf") + "\n" +
                "Obra: " + dadosAtuais.get("obra") + "\n" +
                "Perfil: " + dadosAtuais.get("perfil") + "\n\n" +
                "O que deseja fazer?";

        Object[] botoesAcao = {"✅ PODE SALVAR (Robô)", "📝 SALVEI MANUALMENTE", "✏️ CORRIGIR DADOS"};

        return JOptionPane.showOptionDialog(null, resumo, "Validação de Cadastro",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, botoesAcao, botoesAcao[0]);
    }

    public Map<String, String> coletarDadosUsuario(Map<String, String> dadosExistentes) {
        JTextField campoNome = new JTextField();
        JTextField campoEmail = new JTextField();
        JTextField campoCPF = new JTextField();
        JTextField campoData = new JTextField();
        JTextField campoFuncao = new JTextField();
        JTextField campoObra = new JTextField();
        String[] tipos = {"Almoxarifado", "Equipe de Apoio - CIVIL", "Equipe de Apoio - INSTALL", "Equipe de Apoio - ESTAGIÁRIO/PROD.", "Gerencial", "Gerencial administrador"};
        JComboBox<String> comboTipo = new JComboBox<>(tipos);

        if (dadosExistentes != null) {
            campoNome.setText(dadosExistentes.get("nome"));
            campoEmail.setText(dadosExistentes.get("email"));
            campoCPF.setText(dadosExistentes.get("cpf"));
            campoData.setText(dadosExistentes.get("data"));
            campoFuncao.setText(dadosExistentes.get("funcao"));
            campoObra.setText(dadosExistentes.get("obra"));
            comboTipo.setSelectedItem(dadosExistentes.get("perfil"));

        }

        Object[] formulario = {"Nome:", campoNome, "E-mail:", campoEmail, "CPF:", campoCPF,"Data de Admissão (DD/MM/AAAA)", campoData,
                "Função:", campoFuncao, "Obra:", campoObra, "Perfil:", comboTipo};

        String titulo = (dadosExistentes == null) ? "Novo Usuário" : "CORRIGIR DADOS";
        int op = JOptionPane.showConfirmDialog(null, formulario, titulo, JOptionPane.OK_CANCEL_OPTION);

        if (op != JOptionPane.OK_OPTION) return null;

        Map<String, String> dados = new HashMap<>();
        dados.put("nome", campoNome.getText().trim());
        dados.put("email", campoEmail.getText().trim().toLowerCase());
        dados.put("cpf", campoCPF.getText().trim());
        dados.put("data", campoData.getText().trim());
        dados.put("funcao", campoFuncao.getText().trim());
        dados.put("obra", campoObra.getText().trim().toUpperCase());
        dados.put("perfil", (String) comboTipo.getSelectedItem());
        return dados;
    }
}
