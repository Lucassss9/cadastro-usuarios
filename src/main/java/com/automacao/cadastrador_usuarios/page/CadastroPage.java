package com.automacao.cadastrador_usuarios.page;

import com.automacao.cadastrador_usuarios.ui.Dialogs;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class CadastroPage {

    private final Dialogs dialogs;

    public CadastroPage(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    public void login(WebDriver driver, WebDriverWait wait, String usuarioLogin, String senhaLogin) {
        driver.get("https://manager.cfobras.com.br/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-1"))).sendKeys(usuarioLogin);
        driver.findElement(By.id("password")).sendKeys(senhaLogin);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("user-profile")));
    }

    public void abrirTelaEmpresa(WebDriver driver) throws InterruptedException {
        driver.get("https://manager.cfobras.com.br/geral/empresa");
        Thread.sleep(2000);
    }

    public void clicarEditarOuPedirAjuste(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        try {
            WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@title='Editar' or contains(text(),'Editar')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnEditar);
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Nao achei o botao Editar: " + e);
        }
    }

    public void abrirAbaUsuarios(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        try {
            WebElement abaUsuarios = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[normalize-space()='Usuários']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaUsuarios);
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Nao achei a aba Usuarios: " + e);
        }
    }

    public void garantirAbaUsuarios(WebDriver driver) {
        try {
            if (driver.findElements(By.xpath("//input[@placeholder='Digite seu nome']")).isEmpty()) {
                WebElement aba = driver.findElement(By.xpath("//a[normalize-space()='Usuários']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", aba);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Erro ao garantir a aba Usuarios: " + e);
        }
    }

    public void selecionarObra(WebDriver driver, WebDriverWait wait, String obra) {
        try {
            WebElement caixa = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.multiselect__tags")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", caixa);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", caixa);
            Thread.sleep(300);

            WebElement input = null;
            try {
                input = caixa.findElement(By.cssSelector("input[type='text'], input.multiselect__input"));
            } catch (Exception ignorado) {
            }

            if (input != null) {
                input.clear();
                input.sendKeys(obra);
                Thread.sleep(600);
                try {
                    WebElement opc = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                            "//span[contains(@class,'multiselect__option')]//span[contains(text(),'" + obra + "')]")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opc);
                } catch (Exception ex) {
                    WebElement primeira = driver.findElement(By.cssSelector(
                            ".multiselect__content-wrapper li:first-child span.multiselect__option"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", primeira);
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            System.out.println("Obra '" + obra + "' nao encontrada: " + e);
        }
    }

    public void preencherCamposBasicos(WebDriver driver, WebDriverWait wait, Map<String, String> dados) {
        boolean terceirizado = "true".equalsIgnoreCase(dados.get("terceirizado"));

        preencher(driver, wait, "//input[@placeholder='Digite seu nome']", dados.get("nome"), "nome");
        preencher(driver, wait, "//input[@placeholder='Digite seu e-mail']", dados.get("email"), "e-mail");
        preencher(driver, wait, "//input[@placeholder='Senha']", "123Mudar@", "senha");

        selecionarFuncao(driver, dados.get("funcao"));

        if (!terceirizado) {
            try {
                WebElement cpf = driver.findElement(By.id("input-cpf"));
                cpf.clear();
                cpf.sendKeys(dados.get("cpf"));
            } catch (Exception e) {
                System.out.println("Erro no campo CPF: " + e);
            }
        }
    }

    private void preencher(WebDriver driver, WebDriverWait wait, String xpath, String valor, String nomeDoCampo) {
        if (valor == null) return;
        try {
            WebElement campo = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            campo.clear();
            campo.sendKeys(valor);
        } catch (Exception e) {
            System.out.println("Erro no campo " + nomeDoCampo + ": " + e);
        }
    }

    public void selecionarFuncao(WebDriver driver, String funcao) {
        if (funcao == null || funcao.isBlank()) return;
        try {
            WebElement select = driver.findElement(
                    By.xpath("//select[option[normalize-space()='Selecione a função']]"));
            new Select(select).selectByVisibleText(funcao);
        } catch (Exception e) {
            System.out.println("Erro ao selecionar a funcao '" + funcao + "': " + e);
        }
    }

    public void selecionarPerfil(WebDriver driver, String tipo) {
        if (tipo == null) return;

        String value = "";
        if (tipo.contains("Equipe de Apoio")) value = "engenheiro";
        else if (tipo.equals("Almoxarifado")) value = "almoxarifado";
        else if (tipo.equals("Operacional")) value = "operacional";
        else if (tipo.equals("Portaria")) value = "portaria";
        else if (tipo.equals("Gerencial")) value = "administrador";
        else if (tipo.contains("administrador")) value = "admGerencial";

        try {
            WebElement radio = driver.findElement(By.xpath("//input[@value='" + value + "']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
        } catch (Exception e) {
            System.out.println("Erro ao selecionar o perfil '" + tipo + "': " + e);
        }
    }

    public void marcarPermissoes(WebDriver driver, String tipo) {
        if (tipo == null) return;
        List<String> lista = new ArrayList<>();

        if ("Almoxarifado".equals(tipo)) {
            lista = Arrays.asList("conversor-de-unidades", "empresa-parceira", "equipments",
                    "equipamentos-em-uso", "equipamentos-ociosos", "estoque", "estoque_mínimo",
                    "kit-insumo", "orders", "pedido", "relatorio", "requisicoes-equipamentos",
                    "requisicao", "retorno-equipamentos", "epi");
        } else if ("Operacional".equals(tipo)) {
            lista = Arrays.asList("requisicao");
        } else if (tipo.contains("CIVIL")) {
            lista = Arrays.asList("requisicao");
        } else if (tipo.contains("INSTALL")) {
            lista = Arrays.asList("conversor-de-unidades", "equipments", "equipamentos-em-uso",
                    "equipamentos-ociosos", "estoque", "kit-insumo", "orders", "pedido", "relatorio",
                    "requisicoes-equipamentos", "requisicao", "retorno-equipamentos", "epi");
        } else if (tipo.contains("Engenheiro")) {
            lista = Arrays.asList("conversor-de-unidades", "equipments", "equipamentos-em-uso",
                    "equipamentos-ociosos", "estoque", "estoque_mínimo", "kit-insumo", "orders",
                    "pedido", "relatorio", "requisicoes-equipamentos", "requisicao",
                    "retorno-equipamentos", "empresa-parceira", "epi");
        } else if (tipo.contains("Gerencial")) {
            lista = Arrays.asList("conversor-de-unidades", "estoque_edicao", "empresa-parceira",
                    "equipments", "equipamentos-em-uso", "equipamentos-ociosos", "estatisticas",
                    "estoque", "estoque_mínimo", "lixeira-de-requisicoes", "kit-insumo", "orders",
                    "quantidade_maxima", "pedido", "relatorio", "requisicoes-equipamentos",
                    "requisicao", "retorno-equipamentos", "material-disponivel", "epi");
        }

        for (String v : lista) {
            try {
                WebElement cb = driver.findElement(By.xpath("//input[@value='" + v + "']"));
                if (!cb.isSelected()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
                }
            } catch (Exception e) {
                System.out.println("Erro na permissao '" + v + "': " + e);
            }
        }
    }

    public String verificarErroNaTela(WebDriver driver) {
        try {
            String xpathErro = "//*[contains(text(), 'Request failed') or contains(text(), 'status code 400')"
                    + " or contains(text(), 'já existe') or contains(@class, 'toast-error')"
                    + " or contains(@class, 'alert-danger') or contains(@class, 'error')]";
            List<WebElement> erros = driver.findElements(By.xpath(xpathErro));
            for (WebElement erro : erros) {
                if (erro.isDisplayed() && erro.getText() != null && !erro.getText().isEmpty()) {
                    String msg = erro.getText();
                    if (msg.contains("400") || msg.contains("failed")) return "Erro Sistema/Duplicidade";
                    return msg;
                }
            }
        } catch (Exception ignorado) {
            return null;
        }
        return null;
    }

    public void clicarSalvar(WebDriver driver, WebDriverWait wait) {
        WebElement btnSalvar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Salvar usuário')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnSalvar);
        btnSalvar.click();
    }
}