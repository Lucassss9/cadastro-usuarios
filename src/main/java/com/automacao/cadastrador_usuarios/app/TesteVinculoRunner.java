package com.automacao.cadastrador_usuarios.app;

import com.automacao.cadastrador_usuarios.flow.CadastroPageFlow;
import com.automacao.cadastrador_usuarios.flow.VinculoObrasFlow;
import com.automacao.cadastrador_usuarios.selenium.DriverFactory;
import com.automacao.cadastrador_usuarios.ui.Dialogs;
import com.automacao.cadastrador_usuarios.ui.StepGuard;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.time.Duration;

public class TesteVinculoRunner {

    public static void main(String[] args) throws Exception {
        Dialogs dialogs = new Dialogs();
        DriverFactory factory = new DriverFactory();
        WebDriver driver = factory.createChrome();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        String usuarioLogin = "suporte_sp@cury.net";
        String senhaLogin = "SpSuporte@69";

        CadastroPageFlow login = new CadastroPageFlow(dialogs);
        VinculoObrasFlow vinculo = new VinculoObrasFlow();

        int passo = dialogs.confirmarSimNao("Modo passo-a-passo (OK em cada etapa)?", "Teste vínculo");
        StepGuard steps = new StepGuard(passo == JOptionPane.YES_OPTION);

        try {
            login.login(driver, wait, usuarioLogin, senhaLogin);

            String obra = "DEZ JARDIM";
            String nomeUsuario = "Lucas Gabriel - SP";

            vinculo.abrirObras(driver, wait, steps);
            vinculo.buscarObra(driver, wait, steps, obra);
            vinculo.abrirEditarDaObra(driver, wait, steps, obra);
            vinculo.irAbaUsuarios(driver, wait, steps);
            vinculo.vincularUsuario(driver, wait, steps, nomeUsuario);
            vinculo.voltarParaGeral(driver, wait, steps);
            vinculo.salvarNaAbaGeral(driver, wait, steps);

            dialogs.info("Teste finalizado.");
        } finally {
            try { driver.quit(); } catch (Exception ignored) {}
        }
    }
}
