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
                "Obras: " + dadosAtuais.getOrDefault("obras_todas", dadosAtuais.get("obra")) + "\n" +
                "Perfil: " + dadosAtuais.get("perfil") + "\n" +
                avisos(dadosAtuais) +
                pendenciasTexto(dadosAtuais) +
                "O que deseja fazer?";

        Object[] botoesAcao = {"Robô clica Salvar", "Eu já salvei", "Pular (não salvar)"};

        return JOptionPane.showOptionDialog(null, resumo, "Conferência do Cadastro",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, botoesAcao, botoesAcao[0]);
    }

    public boolean deuCerto(String nome) {
        Object[] opcoes = {"Sim, salvou", "Não / deu erro"};
        int r = JOptionPane.showOptionDialog(null,
                "O cadastro de '" + nome + "' foi salvo com sucesso na tela?",
                "Confirmar salvamento",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
        return r == JOptionPane.YES_OPTION;
    }

    public int oQueFazerComErro(String nome) {
        Object[] opcoes = {"Tentar de novo", "Vou resolver na mão", "Pular este"};
        return JOptionPane.showOptionDialog(null,
                "Deu problema com '" + nome + "'. O que fazer?",
                "Deu erro",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[0]);
    }

    private String pendenciasTexto(Map<String, String> d) {
        String p = d.get("pendencias");
        if (p == null || p.isBlank()) {
            return ">> Conferido: todos os campos OK.\n\n";
        }
        return ">> CONFERENCIA ENCONTROU PROBLEMAS:\n" + p + "\n";
    }

    private String avisos(Map<String, String> d) {
        StringBuilder sb = new StringBuilder();
        String obrasFalha = d.get("obras_falha");
        if (obrasFalha != null && !obrasFalha.isBlank()) {
            sb.append("\n>> ATENCAO: nao achei estas obras na tela: ").append(obrasFalha).append("\n");
        }
        boolean terceirizado = "true".equalsIgnoreCase(d.get("terceirizado"));
        String cpf = d.get("cpf");
        if (!terceirizado && (cpf == null || cpf.isBlank())) {
            sb.append(">> ATENCAO: sem CPF nesta solicitacao.\n");
        }
        if (sb.length() > 0) sb.append("\n");
        return sb.toString();
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