package com.automacao.cadastrador_usuarios.flow;

import com.automacao.cadastrador_usuarios.ui.StepGuard;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class VinculoObrasFlow {

    public void abrirObras(WebDriver driver, WebDriverWait wait, StepGuard steps) {
        steps.step("Vínculo", "Vou abrir o menu Geral.");
        WebElement geral = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class,'side-nav-link')][.//span[normalize-space()='Geral']]")
        ));
        clickJs(driver, geral);
        steps.sleep(400);

        steps.step("Vínculo", "Agora vou clicar em Obras (vai demorar um pouco para carregar, é normal).");
        WebElement obras = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class,'side-nav-link-ref') and contains(@href,'/geral/obra') and normalize-space()='Obras']")
        ));
        clickJs(driver, obras);

        wait.withTimeout(Duration.ofSeconds(90))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("input.form-control[placeholder*='Pesquisar obra']")
                ));

        steps.step("Vínculo", "Tela de Obras carregada. Agora posso pesquisar a obra.");
    }

    public void buscarObra(WebDriver driver, WebDriverWait wait, StepGuard steps, String obraNome) {
        steps.step("Buscar Obra", "Vou digitar a obra no campo de pesquisa: " + obraNome);

        WebElement input = wait.withTimeout(Duration.ofSeconds(90)).until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input.form-control[placeholder*='Pesquisar obra']")
        ));

        scrollIntoView(driver, input);
        clickJs(driver, input);
        clearSafe(driver, input);
        input.sendKeys(obraNome);

        steps.sleep(400);

        steps.step("Buscar Obra", "Agora vou clicar em Buscar.");
        WebElement btnBuscar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(@class,'btn') and contains(@class,'btn-primary') and normalize-space()='Buscar']")
        ));
        clickJs(driver, btnBuscar);

        steps.step("Buscar Obra", "Aguarde o resultado aparecer na lista/cards.");
        wait.withTimeout(Duration.ofSeconds(60)).until(d -> {
            String src = d.getPageSource().toLowerCase();
            return src.contains("editar") || src.contains(obraNome.toLowerCase());
        });

        steps.sleep(700);
    }

    public void abrirEditarDaObra(WebDriver driver, WebDriverWait wait, StepGuard steps, String obraNome) {
        steps.step("Editar Obra", "Vou localizar a obra e clicar em Editar: " + obraNome);

        By btnEditarLocator = By.xpath(
                "//*[contains(@class,'card') or contains(@class,'row') or contains(@class,'col')]" +
                        "[.//*[contains(normalize-space(.), " + xpathLiteral(obraNome) + ")]]" +
                        "//button[@title='Editar' or @aria-label='pencil square' or contains(@class,'btn-secondary')]"
        );

        WebElement btnEditar = wait.withTimeout(Duration.ofSeconds(60))
                .until(ExpectedConditions.elementToBeClickable(btnEditarLocator));

        scrollIntoView(driver, btnEditar);
        clickJs(driver, btnEditar);

        steps.step("Editar Obra", "Entrei em Editar. Vou esperar as abas Geral/Usuários ficarem disponíveis.");
        wait.withTimeout(Duration.ofSeconds(60))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Geral']")
                ));

        steps.sleep(900);
    }

    public void irAbaUsuarios(WebDriver driver, WebDriverWait wait, StepGuard steps) {
        steps.step("Aba Usuários", "Agora SIM vou mudar para a aba Usuários (depois de entrar no Editar).");

        WebElement tabUsuarios = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Usuários']")
        ));

        scrollIntoView(driver, tabUsuarios);
        clickJs(driver, tabUsuarios);

        steps.step("Aba Usuários", "Aguardando a lista de usuários carregar (pode demorar bastante).");

        wait.withTimeout(Duration.ofSeconds(120))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[contains(@class,'tab-pane') and contains(@class,'active')]")
                ));

        wait.withTimeout(Duration.ofSeconds(180)).until(d -> {
            boolean temTabelaDisponivel = !d.findElements(By.xpath("//table//thead//div[contains(.,'Usuário disponível')]")).isEmpty();
            boolean temTabelaVinculado = !d.findElements(By.xpath("//table//thead//div[contains(.,'Usuário vinculado')]")).isEmpty();
            return temTabelaDisponivel && temTabelaVinculado;
        });

        steps.sleep(1200);
    }

    public boolean usuarioJaVinculado(WebDriver driver, String nome) {
        String n = nome == null ? "" : nome.trim();
        if (n.isEmpty()) return false;

        return !driver.findElements(By.xpath(
                "//table[.//thead//div[contains(.,'Usuário vinculado')]]//tbody//td[normalize-space()=" + xpathLiteral(n) + "]"
        )).isEmpty();
    }

    public void vincularUsuario(WebDriver driver, WebDriverWait wait, StepGuard steps, String nome) {
        String n = nome == null ? "" : nome.trim();
        if (n.isEmpty()) return;

        steps.step("Vincular Usuário", "Vou verificar se o usuário já está vinculado: " + n);
        if (usuarioJaVinculado(driver, n)) {
            steps.step("Vincular Usuário", "Já está vinculado. Não vou mexer.");
            return;
        }

        steps.step("Vincular Usuário", "Vou esperar o usuário aparecer em 'Usuário disponível' (pode demorar).");

        By tdDisponivel = By.xpath(
                "//table[.//thead//div[contains(.,'Usuário disponível')]]//tbody//td[normalize-space()=" + xpathLiteral(n) + "]"
        );

        wait.withTimeout(Duration.ofSeconds(180)).until(d ->
                usuarioJaVinculado(d, n) || !d.findElements(tdDisponivel).isEmpty()
        );

        if (usuarioJaVinculado(driver, n)) {
            steps.step("Vincular Usuário", "Enquanto carregava, ele já apareceu como vinculado. OK.");
            return;
        }

        WebElement row = wait.until(ExpectedConditions.elementToBeClickable(tdDisponivel));
        scrollIntoView(driver, row);
        clickJs(driver, row);

        steps.step("Vincular Usuário", "Cliquei no usuário disponível. Vou dar um tempo para ele ir para a lista vinculados.");
        wait.withTimeout(Duration.ofSeconds(60)).until(d -> usuarioJaVinculado(d, n));
        steps.sleep(800);
    }

    public void voltarParaGeral(WebDriver driver, WebDriverWait wait, StepGuard steps) {
        steps.step("Voltar Geral", "Agora vou voltar para a aba Geral (a de baixo, dentro do Editar).");

        WebElement geralTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class,'nav-tabs')]//a[normalize-space()='Geral']")
        ));

        scrollIntoView(driver, geralTab);
        clickJs(driver, geralTab);

        wait.withTimeout(Duration.ofSeconds(60))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[contains(@class,'tab-pane') and contains(@class,'active')]//form")
                ));

        steps.sleep(800);
    }

    public void salvarNaAbaGeral(WebDriver driver, WebDriverWait wait, StepGuard steps) {
        steps.step("Salvar", "Vou procurar o botão Salvar dentro do formulário da aba Geral (de baixo).");

        By salvarBtn = By.xpath(
                "//div[contains(@class,'tab-pane') and contains(@class,'active')]//form//button[@type='submit' and normalize-space()='Salvar']"
        );

        WebElement salvar = wait.withTimeout(Duration.ofSeconds(180))
                .until(ExpectedConditions.elementToBeClickable(salvarBtn));

        scrollIntoView(driver, salvar);
        clickJs(driver, salvar);

        steps.step("Salvar", "Cliquei em Salvar. Vou aguardar finalizar.");
        steps.sleep(1800);
    }

    private void clickJs(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    private void scrollIntoView(WebDriver driver, WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    private void clearSafe(WebDriver driver, WebElement input) {
        try {
            input.clear();
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].value='';", input);
        }
    }

    private static String xpathLiteral(String s) {
        if (s == null) return "''";
        if (!s.contains("'")) return "'" + s + "'";
        if (!s.contains("\"")) return "\"" + s + "\"";
        StringBuilder sb = new StringBuilder("concat(");
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            String ch = String.valueOf(chars[i]);
            if (chars[i] == '\'') sb.append("\"'\"");
            else sb.append("'").append(ch).append("'");
            if (i != chars.length - 1) sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }
}
