package com.automacao.cadastrador_usuarios.app;

import com.automacao.cadastrador_usuarios.page.CadastroPage;
import com.automacao.cadastrador_usuarios.util.ExcelImporter;
import com.automacao.cadastrador_usuarios.util.ReportService;
import com.automacao.cadastrador_usuarios.util.DriverFactory;
import com.automacao.cadastrador_usuarios.ui.Dialogs;
import com.automacao.cadastrador_usuarios.ui.JanelaStatus;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Runner {

    public void start() throws Exception {
        Dialogs dialogs = new Dialogs();
        ExcelImporter excel = new ExcelImporter();
        ReportService report = new ReportService();
        DriverFactory driverFactory = new DriverFactory();

        int escolhaEstado = dialogs.escolherEstado();
        if (escolhaEstado < 0) System.exit(0);

        String usuarioLogin = escolhaEstado == 0 ? "suporte_sp@cury.net" : "suporte_rj@cury.net";
        String senhaLogin = escolhaEstado == 0 ? "SpSuporte@69" : "RjSuporte@69";

        int modo = dialogs.escolherModo();
        if (modo < 0) System.exit(0);

        Queue<Map<String, String>> filaUsuarios = new LinkedList<>();
        boolean modoManualLoop = (modo == 1);

        if (modo == 0) {
            List<Map<String, String>> doExcel = excel.importarExcel();
            if (doExcel == null || doExcel.isEmpty()) {
                dialogs.info("Planilha vazia ou erro ao ler.");
                System.exit(0);
            }
            filaUsuarios.addAll(doExcel);
        } else {
            Map<String, String> primeiro = dialogs.coletarDadosUsuario(null);
            if (primeiro == null) System.exit(0);
            filaUsuarios.add(primeiro);
        }

        JanelaStatus status = new JanelaStatus();
        status.setVisible(true);
        status.atualizar("Abrindo navegador...");

        WebDriver driver = driverFactory.createChrome();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        List<Map<String, String>> listaRelatorio = new ArrayList<>();

        CadastroPage flow = new CadastroPage(dialogs);

        try {
            status.atualizar("Login...");
            flow.login(driver, wait, usuarioLogin, senhaLogin);

            status.atualizar("Abrindo cadastro...");
            flow.abrirTelaEmpresa(driver);
            flow.clicarEditarOuPedirAjuste(driver, wait);
            flow.abrirAbaUsuarios(driver, wait);

            while (!filaUsuarios.isEmpty()) {
                Map<String, String> dadosAtuais = filaUsuarios.poll();
                dadosAtuais.put("data_hora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                status.atualizar("Processando: " + dadosAtuais.getOrDefault("nome", ""));

                boolean usuarioFinalizado = false;

                while (!usuarioFinalizado) {
                    flow.garantirAbaUsuarios(driver);

                    flow.selecionarObra(driver, wait, dadosAtuais.get("obra"));
                    flow.preencherCamposBasicos(driver, wait, dadosAtuais);
                    flow.selecionarPerfil(driver, dadosAtuais.get("perfil"));
                    flow.marcarPermissoes(driver, dadosAtuais.get("perfil"));

                    status.setVisible(false);
                    int acao = dialogs.validarCadastro(dadosAtuais);
                    status.setVisible(true);

                    if (acao == 0) {
                        try {
                            flow.clicarSalvar(driver, wait);
                            Thread.sleep(1500);
                            String erroTela = flow.verificarErroNaTela(driver);

                            if (erroTela != null) {
                                dadosAtuais.put("status_cadastro", "FALHA: " + erroTela);
                                driver.navigate().refresh();
                                Thread.sleep(2000);
                                try {
                                    WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='Editar' or contains(text(),'Editar')]")));
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnEditar);
                                    Thread.sleep(1000);
                                    WebElement abaUser = driver.findElement(By.xpath("//a[contains(text(),'Usuários')] | //li[contains(text(),'Usuários')]"));
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaUser);
                                } catch (Exception ignored) {}
                            } else {
                                dadosAtuais.put("status_cadastro", "SUCESSO");
                                usuarioFinalizado = true;
                            }
                        } catch (Exception e) {
                            dadosAtuais.put("status_cadastro", "ERRO");
                            usuarioFinalizado = true;
                        }
                    } else if (acao == 1) {
                        dadosAtuais.put("status_cadastro", "MANUAL");
                        driver.navigate().refresh();
                        Thread.sleep(2000);
                        try {
                            WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='Editar' or contains(text(),'Editar')]")));
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnEditar);
                            Thread.sleep(1000);
                            WebElement abaUser = driver.findElement(By.xpath("//a[contains(text(),'Usuários')] | //li[contains(text(),'Usuários')]"));
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaUser);
                        } catch (Exception ignored) {}
                        usuarioFinalizado = true;
                    } else if (acao == 2) {
                        status.setVisible(false);
                        Map<String, String> novosDados = dialogs.coletarDadosUsuario(dadosAtuais);
                        status.setVisible(true);
                        if (novosDados != null) {
                            dadosAtuais.putAll(novosDados);
                            driver.navigate().refresh();
                            Thread.sleep(1500);
                            try {
                                WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='Editar' or contains(text(),'Editar')]")));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnEditar);
                                Thread.sleep(1000);
                                WebElement abaUser = driver.findElement(By.xpath("//a[contains(text(),'Usuários')] | //li[contains(text(),'Usuários')]"));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaUser);
                            } catch (Exception ignored) {}
                        }
                    } else {
                        dadosAtuais.put("status_cadastro", "CANCELADO");
                        usuarioFinalizado = true;
                    }
                }

                listaRelatorio.add(dadosAtuais);

                if (modoManualLoop && filaUsuarios.isEmpty()) {
                    status.setVisible(false);
                    int outro = dialogs.confirmarSimNao("Deseja digitar mais um usuário?", "Continuar");
                    status.setVisible(true);
                    if (outro == JOptionPane.YES_OPTION) {
                        Map<String, String> novo = dialogs.coletarDadosUsuario(null);
                        if (novo != null) filaUsuarios.add(novo);
                    }
                }
            }

            status.setVisible(false);

            String path = report.gerarRelatorioDetalhado(listaRelatorio);
            dialogs.info("Finalizado:\n" + path);

        } catch (Exception e) {
            status.setVisible(false);
            dialogs.info("Erro: " + e.getMessage());
        } finally {
            try { driver.quit(); } catch (Exception ignored) {}
            System.exit(0);
        }
    }
}
