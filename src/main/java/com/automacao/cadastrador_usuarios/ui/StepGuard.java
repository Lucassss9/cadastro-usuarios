package com.automacao.cadastrador_usuarios.ui;

import javax.swing.*;

public class StepGuard {

    private final boolean modoPassoAPasso;

    public StepGuard(boolean modoPassoAPasso) {
        this.modoPassoAPasso = modoPassoAPasso;
    }

    public void step(String titulo, String msg) {
        if (!modoPassoAPasso) return;
        JOptionPane.showMessageDialog(null, msg, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
}
