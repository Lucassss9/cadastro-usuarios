package com.automacao.cadastrador_usuarios.app;

import com.automacao.cadastrador_usuarios.page.CadastroPage;
import com.automacao.cadastrador_usuarios.util.DriverFactory;
import com.automacao.cadastrador_usuarios.ui.Dialogs;
import com.automacao.cadastrador_usuarios.util.ModalClose;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TesteCadastroRunner {
    public static void main(String[] args) {
        DriverFactory driverFactory = new DriverFactory();

        WebDriver driver = driverFactory.createChrome();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        CadastroPage flow = new CadastroPage(new Dialogs());
        ModalClose modalClose = new ModalClose();

        String usuarioLogin = "suporte_sp@cury.net";
        String usuarioSenha = "SpSuporte@69";

        try {
            flow.login(driver, wait, usuarioLogin, usuarioSenha);
            modalClose.fecharModal(driver);
            Thread.sleep(1000);

            flow.abrirTelaEmpresa(driver);
            modalClose.fecharModal(driver);
            Thread.sleep(1000);

            flow.clicarEditarOuPedirAjuste(driver, wait);
            modalClose.fecharModal(driver);
            Thread.sleep(1000);

            flow.abrirAbaUsuarios(driver, wait);
            modalClose.fecharModal(driver);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Erro: " + e);
        } finally {
            System.out.println("Logado...");
            driver.quit();
        }
    }
}
