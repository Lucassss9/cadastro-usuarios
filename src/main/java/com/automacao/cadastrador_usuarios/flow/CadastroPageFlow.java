package com.automacao.cadastrador_usuarios.flow;

import com.automacao.cadastrador_usuarios.ui.Dialogs;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class CadastroPageFlow {

    private final Dialogs dialogs;

    public CadastroPageFlow(Dialogs dialogs) {
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
            WebElement btnEditar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@title='Editar' or contains(text(),'Editar')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnEditar);
            Thread.sleep(1000);
        } catch (Exception ignored) {
            dialogs.info("Não achei o botão Editar. Ajuste a tela manualmente e dê OK.");
        }
    }

    public void abrirAbaUsuarios(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        try {
            WebElement abaUsuarios = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Usuários')] | //li[contains(text(),'Usuários')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaUsuarios);
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    public void garantirAbaUsuarios(WebDriver driver) {
        try {
            if (driver.findElements(By.xpath("//input[@placeholder='Digite seu nome']")).isEmpty()) {
                WebElement abaUsuarios = driver.findElement(By.xpath("//a[contains(text(),'Usuários')] | //li[contains(text(),'Usuários')]"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaUsuarios);
                Thread.sleep(1000);
            }
        } catch(Exception ignored) {}
    }

    public void selecionarObra(WebDriver driver, WebDriverWait wait, String obra) {
        try {
            WebElement caixa = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.multiselect__tags")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", caixa);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", caixa);
            Thread.sleep(300);

            WebElement input = null;
            try { input = caixa.findElement(By.cssSelector("input[type='text'], input.multiselect__input")); } catch (Exception ignored) {}

            if (input != null) {
                input.clear();
                input.sendKeys(obra);
                Thread.sleep(600);
                try {
                    WebElement opc = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//span[contains(@class,'multiselect__option')]//span[contains(text(),'" + obra + "')]")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opc);
                } catch(Exception ex) {
                    WebElement first = driver.findElement(By.cssSelector(".multiselect__content-wrapper li:first-child span.multiselect__option"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", first);
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            dialogs.info("A obra '" + obra + "' não encontrada automaticamente.");
        }
    }

    public void preencherCamposBasicos(WebDriver driver, WebDriverWait wait, Map<String, String> dadosAtuais) {
        String dataCompleta = dadosAtuais.get("data");

        String[] partes = dataCompleta.split("/");
        String dia =  partes[0];
        String mes = partes[1];
        String ano = partes[2];

        try {
            WebElement nomeEl = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@placeholder='Digite seu nome']")));
            nomeEl.clear();
            nomeEl.sendKeys(dadosAtuais.get("nome"));
        } catch (Exception ignored) {}

        try {
            WebElement emailEl = driver.findElement(By.xpath("//input[@placeholder='Digite seu e-mail']"));
            emailEl.clear();
            emailEl.sendKeys(dadosAtuais.get("email"));
        } catch (Exception ignored) {}

        try {
            WebElement funcEl = driver.findElement(By.xpath("//input[@placeholder='Digite sua função']"));
            funcEl.clear();
            funcEl.sendKeys(dadosAtuais.get("funcao"));
        } catch (Exception ignored) {}

        try {
            WebElement cpfEl = driver.findElement(By.xpath("//input[@placeholder='Digite seu CPF']"));
            cpfEl.clear();
            cpfEl.sendKeys(dadosAtuais.get("cpf"));
        } catch (Exception ignored) {}

        try {
            WebElement senhaEl = driver.findElement(By.xpath("//input[@placeholder='Senha']"));
            senhaEl.clear();
            senhaEl.sendKeys("123Mudar@");
        } catch (Exception ignored) {}

        try {
            WebElement dataEl = driver.findElement(By.xpath("//label[contains(text(), 'Data de Admissão')]/following-sibling::div//button"));
            dataEl.click();

            Thread.sleep(300);

            WebElement diaParaClicar = driver.findElement(By.xpath("//span[text()='" + dia + "']"));
            diaParaClicar.click();

            WebElement mesParaClicar = driver.findElement(By.xpath("//span[text()='" + mes + "']"));
            mesParaClicar.click();

            WebElement anoParaClicar = driver.findElement(By.xpath("//span[text()='" + ano + "']"));
            anoParaClicar.click();
        } catch (Exception ignored) {}
    }

    public void selecionarPerfil(WebDriver driver, String tipo) {
        String value = "";
        if (tipo == null) return;
        if (tipo.contains("Equipe de Apoio")) value = "engenheiro";
        else if (tipo.equals("Almoxarifado")) value = "almoxarifado";
        else if (tipo.equals("Gerencial")) value = "administrador";
        else if (tipo.contains("administrador")) value = "admGerencial";

        try {
            WebElement radio = driver.findElement(By.xpath("//input[@value='" + value + "']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
        } catch (Exception ignored) {}
    }

    public void marcarPermissoes(WebDriver driver, String tipo) {
        List<String> lista = new ArrayList<>();
        if ("Almoxarifado".equals(tipo)) lista = Arrays.asList("conversor-de-unidades", "empresa-parceira", "equipments", "equipamentos-em-uso", "equipamentos-ociosos", "estoque", "estoque_mínimo", "kit-insumo", "orders", "pedido", "relatorio", "requisicoes-equipamentos", "requisicao", "retorno-equipamentos", "epi");
        else if (tipo != null && tipo.contains("CIVIL")) lista = Arrays.asList("requisicao", "relatorio", "requisicoes-equipamentos");
        else if (tipo != null && tipo.contains("INSTALL")) lista = Arrays.asList("conversor-de-unidades", "equipments", "equipamentos-em-uso", "equipamentos-ociosos", "estoque", "kit-insumo", "orders", "pedido", "relatorio", "requisicoes-equipamentos", "requisicao", "retorno-equipamentos", "epi");
        else if (tipo != null && tipo.contains("ESTAGIÁRIO")) lista = Arrays.asList("conversor-de-unidades", "equipments", "equipamentos-em-uso", "equipamentos-ociosos", "estoque", "estoque_mínimo", "kit-insumo", "orders", "pedido", "relatorio", "requisicoes-equipamentos", "requisicao", "retorno-equipamentos", "empresa-parceira", "epi");
        else if (tipo != null && tipo.contains("Gerencial")) lista = Arrays.asList("conversor-de-unidades", "estoque_edicao", "empresa-parceira", "equipments", "equipamentos-em-uso", "equipamentos-ociosos", "estatisticas", "estoque", "estoque_mínimo", "lixeira-de-requisicoes", "kit-insumo", "orders", "quantidade_maxima", "pedido", "relatorio", "requisicoes-equipamentos", "requisicao", "retorno-equipamentos", "material-disponivel", "epi");

        for (String v : lista) {
            try {
                WebElement cb = driver.findElement(By.xpath("//input[@value='" + v + "']"));
                if (!cb.isSelected()) ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
            } catch (Exception ignored) {}
        }
    }

    public String verificarErroNaTela(WebDriver driver) {
        try {
            String xpathErro = "//*[contains(text(), 'Request failed') or contains(text(), 'status code 400') or contains(text(), 'já existe') or contains(@class, 'toast-error') or contains(@class, 'alert-danger') or contains(@class, 'error')]";
            List<WebElement> erros = driver.findElements(By.xpath(xpathErro));
            for (WebElement erro : erros) {
                if (erro.isDisplayed() && erro.getText() != null && !erro.getText().isEmpty()) {
                    String msg = erro.getText();
                    if (msg.contains("400") || msg.contains("failed")) return "Erro Sistema/Duplicidade";
                    return msg;
                }
            }
        } catch (Exception ignored) { return null; }
        return null;
    }

    public void clicarSalvar(WebDriver driver, WebDriverWait wait) {
        WebElement btnSalvar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Salvar usuário')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnSalvar);
        btnSalvar.click();
    }
}
