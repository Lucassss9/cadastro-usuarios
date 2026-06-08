package com.automacao.cadastrador_usuarios.ui;

import javax.swing.*;
import java.awt.*;

public class JanelaStatus extends JFrame {
    private final JLabel lblMensagem;
    private final JProgressBar barraProgresso;

    public JanelaStatus() {
        setTitle("Robô Cadastrador");
        setSize(450, 110);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(0, 120, 215)));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblMensagem = new JLabel("Iniciando...");
        lblMensagem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMensagem.setHorizontalAlignment(SwingConstants.CENTER);

        barraProgresso = new JProgressBar();
        barraProgresso.setIndeterminate(true);
        barraProgresso.setForeground(new Color(0, 120, 215));

        panel.add(lblMensagem, BorderLayout.CENTER);
        panel.add(barraProgresso, BorderLayout.SOUTH);
        add(panel);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width / 2 - getWidth() / 2, screen.height - getHeight() - 160);
        setAlwaysOnTop(true);
    }

    public void atualizar(String texto) {
        SwingUtilities.invokeLater(() -> lblMensagem.setText(texto));
    }
}
