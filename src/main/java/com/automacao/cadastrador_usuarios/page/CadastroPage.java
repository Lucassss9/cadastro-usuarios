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

    /** Abre o multiselect de filiais (uma vez por cadastro). */
    private void abrirMultiselectFilial(WebDriver driver) throws InterruptedException {
        if (listaVisivel(driver)) return;   // ja aberta: nao clica de novo (senao fecha)
        WebElement caixa = driver.findElement(By.cssSelector("div.multiselect"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", caixa);
        try {
            WebElement seta = caixa.findElement(By.cssSelector("div.multiselect__select"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", seta);
        } catch (Exception e) {
            WebElement tags = caixa.findElement(By.cssSelector("div.multiselect__tags"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tags);
        }
        Thread.sleep(500);
    }

    /**
     * Seleciona UMA obra no multiselect. Pode ser chamado várias vezes (multi-filial).
     * A lista fica aberta entre chamadas; só reabre se tiver fechado.
     */
    public boolean selecionarObra(WebDriver driver, WebDriverWait wait, String obra) {
        String alvo = obra == null ? "" : obra.trim();
        String codigo = primeiraParte(alvo);

        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            try {
                abrirMultiselectFilial(driver);
                Thread.sleep(600);

                WebElement opc = acharOpcao(driver, alvo, codigo);

                if (opc == null) {
                    tentarBuscarNoInput(driver, codigo);
                    Thread.sleep(800);
                    opc = acharOpcao(driver, alvo, codigo);
                }

                if (opc == null) {
                    // tenta buscar por uma palavra do nome (evita o "criar tag" do codigo puro)
                    String palavraNome = palavraDoNome(alvo);
                    if (!palavraNome.isBlank()) {
                        tentarBuscarNoInput(driver, palavraNome);
                        Thread.sleep(800);
                        opc = acharOpcao(driver, alvo, codigo);
                    }
                }

                if (opc == null) {
                    limparBusca(driver);
                    Thread.sleep(400);
                    opc = rolarListaProcurando(driver, alvo, codigo);
                }

                if (opc != null) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});", opc);
                    Thread.sleep(200);
                    try {
                        opc.click();
                    } catch (Exception cliqueNormalFalhou) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opc);
                    }
                    Thread.sleep(600);

                    if (obraFoiMarcada(driver, codigo)) {
                        limparBusca(driver);
                        return true;
                    }
                }

                System.out.println("Obra '" + obra + "' - tentativa " + tentativa + " sem sucesso, repetindo...");
                Thread.sleep(1000);

            } catch (Exception e) {
                System.out.println("Obra '" + obra + "' - tentativa " + tentativa
                        + ": " + e.getClass().getSimpleName());
                try { Thread.sleep(1000); } catch (Exception ig) {}
            }
        }

        System.out.println("Obra '" + obra + "' nao foi selecionada apos 5 tentativas.");
        return false;
    }

    private String palavraDoNome(String obra) {
        if (obra == null) return "";
        int traco = obra.indexOf('-');
        if (traco < 0 || traco + 1 >= obra.length()) return "";
        String nome = obra.substring(traco + 1).trim();
        String[] palavras = nome.split("\\s+");
        for (String p : palavras) {
            if (p.length() >= 4) return p;
        }
        return palavras.length > 0 ? palavras[0] : "";
    }

    private WebElement rolarListaProcurando(WebDriver driver, String alvo, String codigo) {
        try {
            WebElement wrapper = driver.findElement(By.cssSelector(".multiselect__content-wrapper"));
            long alturaTotal = ((Number) ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].scrollHeight;", wrapper)).longValue();
            long passo = 200;
            for (long y = 0; y <= alturaTotal + passo; y += passo) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[1];", wrapper, y);
                Thread.sleep(150);
                WebElement achou = acharOpcao(driver, alvo, codigo);
                if (achou != null) return achou;
            }
        } catch (Exception e) {
            System.out.println("Erro ao rolar a lista de filiais: " + e.getClass().getSimpleName());
        }
        return null;
    }

    private WebElement acharOpcao(WebDriver driver, String alvo, String codigo) {
        String alvoUm = alvo.replaceAll("\\s+", " ");
        for (WebElement el : driver.findElements(By.cssSelector(
                "li.multiselect__element span.multiselect__option"))) {
            String texto = el.getText() == null ? "" : el.getText().trim();

            // ignora a opcao "criar tag" (aparece quando o texto digitado nao e uma obra existente)
            if (texto.toLowerCase().contains("press enter")
                    || texto.toLowerCase().contains("create a tag")
                    || texto.toLowerCase().contains("criar")) {
                continue;
            }

            String textoUm = texto.replaceAll("\\s+", " ");
            // exige que seja a obra REAL: comeca com o codigo E tem mais texto depois (o nome)
            boolean ehObraReal = texto.startsWith(codigo + " ") && texto.length() > codigo.length() + 1;
            if (textoUm.equalsIgnoreCase(alvoUm) || ehObraReal) {
                return el;
            }
        }
        return null;
    }

    private void tentarBuscarNoInput(WebDriver driver, String codigo) {
        try {
            WebElement input = driver.findElement(By.cssSelector("input.multiselect__input"));
            ((JavascriptExecutor) driver).executeScript(
                    "var el=arguments[0], val=arguments[1];"
                            + "el.focus();"
                            + "el.value=val;"
                            + "el.dispatchEvent(new Event('input',{bubbles:true}));"
                            + "el.dispatchEvent(new Event('keyup',{bubbles:true}));"
                            + "el.dispatchEvent(new Event('change',{bubbles:true}));",
                    input, codigo);
        } catch (Exception ignorado) {
        }
    }

    private void limparBusca(WebDriver driver) {
        try {
            WebElement input = driver.findElement(By.cssSelector("input.multiselect__input"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value='';"
                            + "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));", input);
        } catch (Exception ignorado) {
        }
    }

    private boolean obraFoiMarcada(WebDriver driver, String codigo) {
        try {
            String cod = codigo == null ? "" : codigo.trim().toUpperCase();
            List<String> tagsVistas = new ArrayList<>();
            for (WebElement tag : driver.findElements(By.cssSelector(
                    ".multiselect__tags-wrap .multiselect__tag, span.multiselect__tag"))) {
                String texto = tag.getText() == null ? "" : tag.getText().trim();
                if (!texto.isBlank()) tagsVistas.add(texto);
                String t = texto.toUpperCase();
                if (!cod.isBlank() && (t.contains(cod)
                        || t.replaceAll("\\s+", "").contains(cod.replaceAll("\\s+", "")))) {
                    return true;
                }
            }
            System.out.println("      obraFoiMarcada: procurei '" + cod + "' nas tags e nao achei. Tags: " + tagsVistas);
        } catch (Exception ignorado) {
        }
        return false;
    }

    private boolean listaVisivel(WebDriver driver) {
        try {
            WebElement wrap = driver.findElement(By.cssSelector(".multiselect__content-wrapper"));
            return wrap.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Pega o código da obra (ex.: "CCISA118") para casar mesmo com espaçamento estranho. */
    private String primeiraParte(String obra) {
        if (obra == null || obra.isBlank()) return "";
        String[] p = obra.trim().split("\\s+");
        return p.length > 0 ? p[0] : obra.trim();
    }

    public void preencherCamposBasicos(WebDriver driver, WebDriverWait wait, Map<String, String> dados) {
        boolean terceirizado = "true".equalsIgnoreCase(dados.get("terceirizado"));

        preencher(driver, wait, "//input[@placeholder='Digite seu nome']", dados.get("nome"), "nome");
        preencher(driver, wait, "//input[@placeholder='Digite seu e-mail']", dados.get("email"), "e-mail");
        String senha = dados.get("senha_padrao");
        if (senha == null || senha.isBlank()) senha = "123Mudar@";
        preencher(driver, wait, "//input[@placeholder='Senha']", senha, "senha");

        selecionarFuncao(driver, dados.get("funcao"));

        if (!terceirizado) {
            String valorCpf = dados.get("cpf");
            if (valorCpf == null || valorCpf.isBlank()) {
                System.out.println("ATENCAO: solicitacao sem CPF; campo ficou vazio.");
            } else {
                preencherPorId(driver, wait, "input-cpf", valorCpf, "CPF");
            }
        }
    }

    private void preencherPorId(WebDriver driver, WebDriverWait wait, String id, String valor, String nome) {
        if (valor == null) return;
        for (int t = 1; t <= 5; t++) {
            try {
                WebElement campo = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", campo);
                campo.clear();
                campo.sendKeys(valor);
                if (valor.equals(campo.getAttribute("value"))) return;
            } catch (Exception e) {
                try { Thread.sleep(800); } catch (Exception ig) {}
            }
        }
        System.out.println("Nao consegui preencher " + nome + " apos 5 tentativas.");
    }

    /**
     * Relê cada campo na tela e devolve a lista do que NAO ficou certo.
     * Vazio = tudo conferido. Tenta corrigir o que faltou antes de reportar.
     */
    public String conferirTudo(WebDriver driver, WebDriverWait wait, Map<String, String> dados) {
        StringBuilder pendencias = new StringBuilder();
        boolean terceirizado = "true".equalsIgnoreCase(dados.get("terceirizado"));

        conferirCampo(driver, wait, "//input[@placeholder='Digite seu nome']", null,
                dados.get("nome"), "Nome", pendencias);
        conferirCampo(driver, wait, "//input[@placeholder='Digite seu e-mail']", null,
                dados.get("email"), "E-mail", pendencias);
        conferirCampo(driver, wait, null, "input-cpf",
                terceirizado ? null : dados.get("cpf"), "CPF", pendencias);

        String funcao = dados.get("funcao");
        if (funcao != null && !funcao.isBlank()) {
            if (!funcaoEstaSelecionada(driver, funcao)) {
                selecionarFuncao(driver, funcao);
                if (!funcaoEstaSelecionada(driver, funcao)) {
                    pendencias.append("- Funcao '").append(funcao).append("' nao selecionou\n");
                }
            }
        }

        String perfil = dados.get("perfil");
        if (perfil != null && !perfil.isBlank()) {
            if (!perfilEstaMarcado(driver, perfil)) {
                selecionarPerfil(driver, perfil);
                marcarPermissoes(driver, perfil);
                if (!perfilEstaMarcado(driver, perfil)) {
                    pendencias.append("- Perfil '").append(perfil).append("' nao marcou\n");
                }
            }
        }

        String obras = dados.getOrDefault("obras_todas", dados.get("obra"));
        if (obras != null && !obras.isBlank()) {
            for (String uma : obras.split(" ; ")) {
                String cod = primeiraParte(uma.trim());
                if (!obraFoiMarcada(driver, cod)) {
                    selecionarObra(driver, wait, uma.trim());
                    if (!obraFoiMarcada(driver, cod)) {
                        pendencias.append("- Obra '").append(uma.trim()).append("' nao entrou\n");
                    }
                }
            }
        }

        return pendencias.toString();
    }

    private void conferirCampo(WebDriver driver, WebDriverWait wait, String xpath, String id,
                               String esperado, String nome, StringBuilder pendencias) {
        if (esperado == null || esperado.isBlank()) return;
        try {
            WebElement campo = (id != null)
                    ? driver.findElement(By.id(id))
                    : driver.findElement(By.xpath(xpath));
            String atual = campo.getAttribute("value");
            if (!esperado.equals(atual)) {
                if (id != null) preencherPorId(driver, wait, id, esperado, nome);
                else preencher(driver, wait, xpath, esperado, nome);
                atual = campo.getAttribute("value");
                if (!esperado.equals(atual)) {
                    pendencias.append("- ").append(nome).append(" esta '")
                            .append(atual).append("', deveria ser '").append(esperado).append("'\n");
                }
            }
        } catch (Exception e) {
            pendencias.append("- ").append(nome).append(" nao pude conferir\n");
        }
    }

    private boolean funcaoEstaSelecionada(WebDriver driver, String funcao) {
        try {
            WebElement select = driver.findElement(
                    By.xpath("//select[option[normalize-space()='Selecione a função']]"));
            String escolhido = new Select(select).getFirstSelectedOption().getText();
            return simplificar(escolhido).equals(simplificar(funcao));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean perfilEstaMarcado(WebDriver driver, String tipo) {
        String value = valorDoPerfil(tipo);
        if (value.isBlank()) return false;
        try {
            return driver.findElement(By.xpath("//input[@value='" + value + "']")).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    private void preencher(WebDriver driver, WebDriverWait wait, String xpath, String valor, String nomeDoCampo) {
        if (valor == null) return;
        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            try {
                WebElement campo = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", campo);
                campo.clear();
                campo.sendKeys(valor);
                if (valor.equals(campo.getAttribute("value"))) return;
            } catch (Exception e) {
                try { Thread.sleep(800); } catch (Exception ig) {}
            }
        }
        System.out.println("Nao consegui preencher o campo " + nomeDoCampo + " apos 5 tentativas.");
    }

    public void selecionarFuncao(WebDriver driver, String funcao) {
        if (funcao == null || funcao.isBlank()) return;
        try {
            WebElement select = driver.findElement(
                    By.xpath("//select[option[normalize-space()='Selecione a função']]"));
            Select combo = new Select(select);
            try {
                combo.selectByVisibleText(funcao);
                return;
            } catch (Exception naoExato) {
                String alvo = simplificar(funcao);
                for (WebElement op : combo.getOptions()) {
                    if (simplificar(op.getText()).equals(alvo)) {
                        combo.selectByVisibleText(op.getText());
                        return;
                    }
                }
                System.out.println("ATENCAO: funcao '" + funcao + "' nao existe na lista do CF Obras.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao selecionar a funcao '" + funcao + "': " + e.getClass().getSimpleName());
        }
    }

    private String simplificar(String texto) {
        if (texto == null) return "";
        String t = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return t.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String valorDoPerfil(String tipo) {
        if (tipo == null) return "";
        if (tipo.contains("Equipe de Apoio")) return "engenheiro";
        if (tipo.equals("Almoxarifado")) return "almoxarifado";
        if (tipo.equals("Operacional")) return "operacional";
        if (tipo.equals("Portaria")) return "portaria";
        if (tipo.equals("Gerencial")) return "administrador";
        if (tipo.contains("administrador")) return "admGerencial";
        return "";
    }

    public void selecionarPerfil(WebDriver driver, String tipo) {
        if (tipo == null) return;
        String value = valorDoPerfil(tipo);

        if (value.isBlank()) {
            System.out.println("ATENCAO: perfil '" + tipo + "' nao reconhecido; nenhum tipo marcado.");
            return;
        }
        try {
            WebElement radio = driver.findElement(By.xpath("//input[@value='" + value + "']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", radio);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radio);
        } catch (Exception e) {
            System.out.println("Erro ao selecionar o perfil '" + tipo + "': " + e.getClass().getSimpleName());
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

    public boolean jaExiste(WebDriver driver) {
        try {
            org.openqa.selenium.Alert alerta = driver.switchTo().alert();
            String texto = alerta.getText() == null ? "" : alerta.getText().toLowerCase();
            boolean duplicado = texto.contains("existe") || texto.contains("cadastrad")
                    || texto.contains("duplicad") || texto.contains("ja possui");
            alerta.accept();
            if (duplicado) return true;
        } catch (Exception semAlerta) {
        }

        try {
            for (WebElement el : driver.findElements(By.xpath(
                    "//*[contains(text(),'existe') or contains(text(),'cadastrado') "
                            + "or contains(text(),'já possui') or contains(text(),'duplicad')]"))) {
                if (el.isDisplayed()) {
                    String t = el.getText().toLowerCase();
                    if (t.contains("existe") || t.contains("cadastrad")
                            || t.contains("possui") || t.contains("duplicad")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignorado) {
        }
        return false;
    }

    public boolean confirmarSalvou(WebDriver driver, String email) {
        try {
            Thread.sleep(1500);
            String fonte = driver.getPageSource();
            if (email != null && !email.isBlank() && fonte.contains(email)) {
                return true;
            }
            return driver.findElements(By.xpath(
                    "//*[contains(text(),'sucesso') or contains(text(),'Sucesso') "
                            + "or contains(text(),'cadastrado') or contains(text(),'salvo')]")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void clicarSalvar(WebDriver driver, WebDriverWait wait) {
        WebElement btnSalvar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Salvar usuário')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btnSalvar);
        btnSalvar.click();
    }
}