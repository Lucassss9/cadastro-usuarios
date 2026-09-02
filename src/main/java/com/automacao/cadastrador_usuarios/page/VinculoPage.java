package com.automacao.cadastrador_usuarios.page;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class VinculoPage {

    private static final String MARCA_INSTALL = "- INSTALL";

    public boolean ehInstall(String nomeDaObra) {
        return nomeDaObra != null && nomeDaObra.toUpperCase().contains(MARCA_INSTALL);
    }

    public boolean setorBateComObra(String setor, String nomeDaObra) {
        String s = setor == null ? "" : setor.trim().toLowerCase();
        boolean install = ehInstall(nomeDaObra);
        if (s.equals("ambos")) return true;
        if (s.equals("install")) return install;
        if (s.equals("civil")) return !install;
        return false;
    }

    private String codigoDaFilial(String textoFilial) {
        if (textoFilial == null || textoFilial.isBlank()) return "";
        String t = textoFilial.trim();
        int traco = t.indexOf('-');
        return (traco > 0 ? t.substring(0, traco) : t.split("\\s+")[0]).trim();
    }

    public boolean selecionarFilial(WebDriver driver, WebDriverWait wait, String codigoFilial) {
        String codigo = codigoDaFilial(codigoFilial);
        if (codigo.isBlank()) {
            System.out.println("Filial sem codigo valido: '" + codigoFilial + "'");
            return false;
        }

        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            try {
                if (filialSelecionadaConfere(driver, codigo)) {
                    System.out.println("Filial " + codigo + " ja estava selecionada.");
                    return true;
                }

                WebElement caixa = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("div.user-profile")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", caixa);
                Thread.sleep(700);

                WebElement opcao = acharOpcaoDeFilial(driver, codigo);
                if (opcao == null) {
                    tentarBuscarFilial(driver, codigo);
                    Thread.sleep(700);
                    opcao = acharOpcaoDeFilial(driver, codigo);
                }

                if (opcao != null) {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});", opcao);
                    Thread.sleep(250);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opcao);
                    Thread.sleep(1000);

                    if (confirmarFilialComEstabilidade(driver, codigo)) {
                        System.out.println("Filial " + codigo + " selecionada e confirmada.");
                        return true;
                    }
                }

                System.out.println("Filial '" + codigo + "' - tentativa " + tentativa + " nao confirmou, repetindo...");
                Thread.sleep(800);
            } catch (Exception e) {
                System.out.println("Filial '" + codigo + "' - tentativa " + tentativa
                        + ": " + e.getClass().getSimpleName());
                try { Thread.sleep(800); } catch (Exception ig) {}
            }
        }
        System.out.println("Filial '" + codigo + "' nao foi selecionada apos 5 tentativas.");
        return false;
    }

    private boolean confirmarFilialComEstabilidade(WebDriver driver, String codigo) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            if (!filialSelecionadaConfere(driver, codigo)) return false;
            Thread.sleep(300);
        }
        return true;
    }

    private boolean filialSelecionadaConfere(WebDriver driver, String codigo) {
        try {
            WebElement perfil = driver.findElement(By.cssSelector("div.user-profile"));
            String texto = perfil.getText() == null ? "" : perfil.getText();
            return texto.toUpperCase().contains(codigo.toUpperCase());
        } catch (Exception e) {
            return false;
        }
    }

    private WebElement acharOpcaoDeFilial(WebDriver driver, String codigo) {
        List<WebElement> candidatos = new ArrayList<>();
        candidatos.addAll(driver.findElements(By.cssSelector("li.multiselect__element span.multiselect__option")));
        candidatos.addAll(driver.findElements(By.cssSelector("a.dropdown-item")));
        candidatos.addAll(driver.findElements(By.cssSelector("div.dropdown-item")));
        candidatos.addAll(driver.findElements(By.xpath("//li[contains(@class,'dropdown')]//a")));

        for (WebElement el : candidatos) {
            try {
                String texto = el.getText() == null ? "" : el.getText().trim();
                if (texto.isBlank()) continue;
                if (texto.toLowerCase().contains("selecione")) continue;
                String t = texto.toUpperCase();
                String c = codigo.toUpperCase();
                if (t.startsWith(c + " ") || t.startsWith(c + "-") || t.equals(c) || t.contains(c + " ")) {
                    return el;
                }
            } catch (Exception ignorado) {
            }
        }
        return null;
    }

    private void tentarBuscarFilial(WebDriver driver, String codigo) {
        try {
            List<WebElement> inputs = driver.findElements(By.cssSelector(
                    "input.multiselect__input, input[type='search'], input[type='text']"));
            for (WebElement input : inputs) {
                if (input.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript(
                            "var el=arguments[0], val=arguments[1];"
                                    + "el.focus(); el.value=val;"
                                    + "el.dispatchEvent(new Event('input',{bubbles:true}));"
                                    + "el.dispatchEvent(new Event('keyup',{bubbles:true}));",
                            input, codigo);
                    return;
                }
            }
        } catch (Exception ignorado) {
        }
    }

    public void abrirTelaObras(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        driver.get("https://manager.cfobras.com.br/geral/obra");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table//th//div[contains(normalize-space(),'Nome da obra')]")));
        esperarObrasCarregarem(driver);
    }

    public void esperarObrasCarregarem(WebDriver driver) throws InterruptedException {
        int estavel = 0;
        int anterior = -1;
        for (int i = 0; i < 30; i++) {
            int qtd = contarLinhasDeObra(driver);
            if (qtd > 0 && qtd == anterior) {
                estavel++;
                if (estavel >= 2) return;
            } else {
                estavel = 0;
            }
            anterior = qtd;
            Thread.sleep(350);
        }
    }

    private int contarLinhasDeObra(WebDriver driver) {
        try {
            return driver.findElements(
                    By.xpath("//table//tbody//tr/td[@aria-colindex='1']")).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> listarObrasDaTela(WebDriver driver) {
        List<String> nomes = new ArrayList<>();
        try {
            for (WebElement c : driver.findElements(
                    By.xpath("//table//tbody//tr/td[@aria-colindex='1']"))) {
                String nome = c.getText() == null ? "" : c.getText().trim();
                if (!nome.isBlank()) nomes.add(nome);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar obras da tela: " + e.getClass().getSimpleName());
        }
        return nomes;
    }

    public boolean abrirEdicaoDaObra(WebDriver driver, WebDriverWait wait, String nomeDaObra) {
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            try {
                WebElement botaoEditar = driver.findElement(By.xpath(
                        "//table//tbody//tr[td[@aria-colindex='1' and normalize-space(text())='"
                                + nomeDaObra + "']]//button[@title='Editar']"));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", botaoEditar);
                Thread.sleep(250);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", botaoEditar);

                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//a[@role='tab' and normalize-space(text())='Usuários']")));
                Thread.sleep(600);

                if (confereObraAberta(driver, nomeDaObra)) {
                    return true;
                }
                System.out.println("Abri edicao mas o nome nao bateu com '" + nomeDaObra
                        + "' (tentativa " + tentativa + ")");
            } catch (Exception e) {
                System.out.println("Nao abri edicao da obra '" + nomeDaObra + "' (tentativa "
                        + tentativa + "): " + e.getClass().getSimpleName());
            }
            try { Thread.sleep(700); } catch (Exception ig) {}
        }
        return false;
    }

    private boolean confereObraAberta(WebDriver driver, String nomeDaObra) {
        try {
            WebElement campoNome = driver.findElement(By.id("input-nome"));
            String valor = campoNome.getAttribute("value");
            if (valor == null) valor = "";
            return valor.trim().equalsIgnoreCase(nomeDaObra.trim())
                    || valor.trim().toUpperCase().contains(nomeDaObra.trim().toUpperCase());
        } catch (Exception e) {
            return false;
        }
    }

    public void abrirAbaUsuarios(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        WebElement aba = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@role='tab' and normalize-space(text())='Usuários']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", aba);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//th//div[normalize-space(text())='Usuário disponível']")));
        esperarListaDeUsuariosCarregar(driver);
    }

    private void esperarListaDeUsuariosCarregar(WebDriver driver) throws InterruptedException {
        int estavel = 0;
        int anterior = -1;
        for (int i = 0; i < 24; i++) {
            int qtd = 0;
            try {
                qtd = driver.findElements(By.xpath(
                        "//table[.//th//div[normalize-space(text())='Usuário disponível']]//tbody//tr")).size();
            } catch (Exception ignorado) {
            }
            if (qtd > 0 && qtd == anterior) {
                estavel++;
                if (estavel >= 2) return;
            } else {
                estavel = 0;
            }
            anterior = qtd;
            Thread.sleep(300);
        }
    }

    public boolean jaEstaVinculado(WebDriver driver, String nome) {
        return existeNaColuna(driver, "Usuário vinculado", nome);
    }

    public boolean estaDisponivel(WebDriver driver, String nome) {
        return existeNaColuna(driver, "Usuário disponível", nome);
    }

    public String vincularPessoa(WebDriver driver, WebDriverWait wait, String nome) {
        try {
            if (jaEstaVinculado(driver, nome)) {
                System.out.println("   [" + nome + "] ja estava vinculado.");
                return "ja";
            }
            WebElement linha = linhaNaColuna(driver, "Usuário disponível", nome);
            if (linha == null) {
                System.out.println("   [" + nome + "] NAO encontrado na lista de disponiveis.");
                return "nao_achei";
            }
            System.out.println("   [" + nome + "] achei em disponivel, clicando...");
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", linha);
            Thread.sleep(250);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", linha);

            for (int espera = 0; espera < 16; espera++) {
                Thread.sleep(350);
                if (jaEstaVinculado(driver, nome)) {
                    System.out.println("   [" + nome + "] vinculado com sucesso.");
                    return "ok";
                }
            }
            System.out.println("   [" + nome + "] cliquei mas NAO apareceu em vinculado.");
            return "falhou";
        } catch (Exception e) {
            System.out.println("   [" + nome + "] ERRO ao vincular: " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
            return "falhou";
        }
    }

    public boolean salvarObra(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement abaGeral = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[@role='tab' and normalize-space(text())='Geral']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", abaGeral);
            Thread.sleep(500);

            WebElement botaoSalvar = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@type='submit' and normalize-space(text())='Salvar']")));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", botaoSalvar);
            Thread.sleep(250);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", botaoSalvar);
            Thread.sleep(1800);
            return true;
        } catch (Exception e) {
            System.out.println("Erro ao salvar a obra: " + e.getClass().getSimpleName());
            return false;
        }
    }

    public void voltarParaLista(WebDriver driver, WebDriverWait wait) {
        try {
            List<WebElement> botoesLista = driver.findElements(
                    By.xpath("//button[.//span[normalize-space(text())='Lista'] or normalize-space(text())='Lista']"));
            for (WebElement b : botoesLista) {
                if (b.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", b);
                    Thread.sleep(1000);
                    return;
                }
            }
            driver.get("https://manager.cfobras.com.br/geral/obra");
            Thread.sleep(1200);
        } catch (Exception e) {
            System.out.println("Erro ao voltar para a lista: " + e.getClass().getSimpleName());
        }
    }

    private WebElement colunaPorCabecalho(WebDriver driver, String textoCabecalho) {
        return driver.findElement(By.xpath(
                "//table[.//th//div[normalize-space(text())='" + textoCabecalho + "']]"));
    }

    private WebElement linhaNaColuna(WebDriver driver, String cabecalho, String nome) {
        try {
            WebElement tabela = colunaPorCabecalho(driver, cabecalho);
            String alvo = normalizar(nome);
            for (WebElement c : tabela.findElements(By.xpath(".//tbody//td"))) {
                String txt = normalizar(c.getText());
                if (txt.equals(alvo) || txt.contains(alvo) || alvo.contains(txt)) {
                    return c;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    public List<String> nomesNaColuna(WebDriver driver, String cabecalho) {
        List<String> nomes = new ArrayList<>();
        try {
            WebElement tabela = colunaPorCabecalho(driver, cabecalho);
            for (WebElement c : tabela.findElements(By.xpath(".//tbody//td"))) {
                String t = c.getText() == null ? "" : c.getText().trim();
                if (!t.isBlank()) nomes.add(t);
            }
        } catch (Exception ignorado) {
        }
        return nomes;
    }

    private boolean existeNaColuna(WebDriver driver, String cabecalho, String nome) {
        return linhaNaColuna(driver, cabecalho, nome) != null;
    }
}