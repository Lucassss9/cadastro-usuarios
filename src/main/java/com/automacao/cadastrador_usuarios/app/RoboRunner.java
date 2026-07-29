package com.automacao.cadastrador_usuarios.app;

import com.automacao.cadastrador_usuarios.page.CadastroPage;
import com.automacao.cadastrador_usuarios.ui.Dialogs;
import com.automacao.cadastrador_usuarios.ui.JanelaStatus;
import com.automacao.cadastrador_usuarios.util.ApiClient;
import com.automacao.cadastrador_usuarios.util.DriverFactory;
import com.automacao.cadastrador_usuarios.util.ModalClose;
import com.automacao.cadastrador_usuarios.util.ReportService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoboRunner {

    private static final String URL_BACKEND = env("CFOBRAS_API_URL", "https://chat-bot-cfobras.onrender.com");
    private static final String ADMIN_EMAIL = env("CFOBRAS_ADMIN_EMAIL", "");
    private static final String ADMIN_SENHA = env("CFOBRAS_ADMIN_SENHA", "");

    private static final String LOGIN_SP = env("CF_LOGIN_SP", "");
    private static final String SENHA_SP = env("CF_SENHA_SP", "");
    private static final String LOGIN_RJ = env("CF_LOGIN_RJ", "");
    private static final String SENHA_RJ = env("CF_SENHA_RJ", "");

    public static void main(String[] args) {
        if (ADMIN_EMAIL.isBlank() || ADMIN_SENHA.isBlank()) {
            System.out.println("Configure CFOBRAS_ADMIN_EMAIL e CFOBRAS_ADMIN_SENHA antes de rodar.");
            return;
        }

        ApiClient api = new ApiClient(URL_BACKEND);
        Dialogs dialogs = new Dialogs();
        ReportService report = new ReportService();
        List<Map<String, String>> fila;

        try {
            api.login(ADMIN_EMAIL, ADMIN_SENHA);
            fila = api.buscarPendentes();
        } catch (Exception e) {
            dialogs.info("Nao consegui falar com o backend: " + e.getMessage());
            return;
        }

        if (fila.isEmpty()) {
            dialogs.info("Nada na fila. Nenhum cadastro aprovado esperando.");
            return;
        }

        JanelaStatus status = new JanelaStatus();
        status.setVisible(true);
        status.atualizar("Abrindo navegador...");

        WebDriver driver = new DriverFactory().createChrome();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        CadastroPage pagina = new CadastroPage(dialogs);
        ModalClose modal = new ModalClose();

        List<Map<String, String>> relatorio = new ArrayList<>();
        String estadoLogado = null;
        int ok = 0;
        int falhou = 0;

        try {
            for (Map<String, String> item : fila) {
                String id = item.get("id");
                String nome = item.get("nome");
                item.put("data_hora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                status.atualizar("Processando: " + nome);

                try {
                    if ("true".equalsIgnoreCase(item.get("ja_tem_acesso"))) {
                        item.put("status_cadastro", "SO VINCULO (ja tem acesso)");
                        api.atualizarStatus(id, "cadastrado", null);
                        relatorio.add(item);
                        falhou++;
                        continue;
                    }

                    String perfil = item.get("perfil");
                    if (perfil == null || perfil.isBlank()) {
                        String msg = "Sem perfil definido (o admin precisa escolher ao aprovar)";
                        item.put("status_cadastro", "FALHA: " + msg);
                        api.atualizarStatus(id, "erro", msg);
                        relatorio.add(item);
                        falhou++;
                        continue;
                    }

                    String estado = item.get("estado");
                    if (!estado.equals(estadoLogado)) {
                        status.atualizar("Entrando no CF Obras como " + estado + "...");
                        entrar(pagina, modal, driver, wait, estado);
                        estadoLogado = estado;
                    }

                    pagina.garantirAbaUsuarios(driver);

                    StringBuilder obrasComProblema = new StringBuilder();
                    String todas = item.get("obras_todas");
                    if (todas != null && !todas.isBlank()) {
                        for (String umaObra : todas.split(" ; ")) {
                            boolean achou = pagina.selecionarObra(driver, wait, umaObra.trim());
                            if (!achou) obrasComProblema.append(umaObra.trim()).append("; ");
                        }
                    } else {
                        boolean achou = pagina.selecionarObra(driver, wait, item.get("obra"));
                        if (!achou) obrasComProblema.append(item.get("obra")).append("; ");
                    }
                    if (obrasComProblema.length() > 0) {
                        item.put("obras_falha", obrasComProblema.toString());
                    }

                    pagina.preencherCamposBasicos(driver, wait, item);
                    pagina.selecionarPerfil(driver, perfil);
                    pagina.marcarPermissoes(driver, perfil);

                    status.atualizar("Conferindo campo por campo...");
                    String pendencias = pagina.conferirTudo(driver, wait, item);
                    if (pendencias != null && !pendencias.isBlank()) {
                        item.put("pendencias", pendencias);
                    }

                    status.setVisible(false);
                    int acao = dialogs.validarCadastro(item);
                    status.setVisible(true);

                    if (acao == 0) {
                        pagina.clicarSalvar(driver, wait);
                        Thread.sleep(2000);

                        if (pagina.jaExiste(driver)) {
                            item.put("status_cadastro", "JA EXISTE (falta vincular)");
                            api.atualizarStatus(id, "erro", "Pessoa ja tem cadastro no CF Obras - falta so vincular");
                            falhou++;
                            relatorio.add(item);
                            driver.navigate().refresh();
                            Thread.sleep(2000);
                            prepararTela(pagina, modal, driver, wait);
                            continue;
                        }

                        status.setVisible(false);
                        boolean deuCerto = dialogs.deuCerto(nome);
                        status.setVisible(true);

                        while (!deuCerto) {
                            status.setVisible(false);
                            int oQueFazer = dialogs.oQueFazerComErro(nome);
                            status.setVisible(true);

                            if (oQueFazer == 0) {
                                status.atualizar("Refazendo: " + nome);
                                driver.navigate().refresh();
                                Thread.sleep(2000);
                                prepararTela(pagina, modal, driver, wait);
                                pagina.garantirAbaUsuarios(driver);
                                String todasR = item.get("obras_todas");
                                if (todasR != null && !todasR.isBlank()) {
                                    for (String uma : todasR.split(" ; ")) pagina.selecionarObra(driver, wait, uma.trim());
                                } else {
                                    pagina.selecionarObra(driver, wait, item.get("obra"));
                                }
                                pagina.preencherCamposBasicos(driver, wait, item);
                                pagina.selecionarPerfil(driver, perfil);
                                pagina.marcarPermissoes(driver, perfil);
                                pagina.conferirTudo(driver, wait, item);
                                pagina.clicarSalvar(driver, wait);
                                Thread.sleep(2000);
                                status.setVisible(false);
                                deuCerto = dialogs.deuCerto(nome);
                                status.setVisible(true);
                            } else if (oQueFazer == 1) {
                                item.put("status_cadastro", "SALVO MANUALMENTE (apos erro)");
                                api.atualizarStatus(id, "cadastrado", null);
                                ok++;
                                break;
                            } else {
                                item.put("status_cadastro", "PULADO (deu erro)");
                                api.atualizarStatus(id, "erro", "Deu erro e foi pulado pelo admin");
                                falhou++;
                                break;
                            }
                        }

                        if (deuCerto) {
                            item.put("status_cadastro", "SUCESSO (robo salvou)");
                            api.atualizarStatus(id, "cadastrado", null);
                            ok++;
                        }
                    } else if (acao == 1) {
                        item.put("status_cadastro", "SALVO MANUALMENTE");
                        api.atualizarStatus(id, "cadastrado", null);
                        ok++;
                    } else {
                        item.put("status_cadastro", "NAO SALVO");
                        api.atualizarStatus(id, "erro", "Conferido mas nao salvo pelo admin");
                        falhou++;
                    }

                    relatorio.add(item);
                    driver.navigate().refresh();
                    Thread.sleep(2000);
                    prepararTela(pagina, modal, driver, wait);

                } catch (Exception e) {
                    item.put("status_cadastro", "ERRO: " + e.getMessage());
                    relatorio.add(item);
                    try {
                        api.atualizarStatus(id, "erro", String.valueOf(e.getMessage()));
                    } catch (Exception ignorado) {
                    }
                    falhou++;
                    try {
                        driver.navigate().refresh();
                        Thread.sleep(2000);
                        prepararTela(pagina, modal, driver, wait);
                    } catch (Exception ignorado) {
                    }
                }
            }
        } finally {
            status.setVisible(false);
            String caminho = report.gerarRelatorioDetalhado(relatorio);
            dialogs.info("Finalizado: " + ok + " ok, " + falhou + " com problema.\nRelatorio: " + caminho);
            try {
                driver.quit();
            } catch (Exception ignorado) {
            }
        }
    }

    private static void entrar(CadastroPage pagina, ModalClose modal, WebDriver driver,
                               WebDriverWait wait, String estado) throws Exception {
        String login = "RJ".equalsIgnoreCase(estado) ? LOGIN_RJ : LOGIN_SP;
        String senha = "RJ".equalsIgnoreCase(estado) ? SENHA_RJ : SENHA_SP;

        if (login.isBlank() || senha.isBlank()) {
            throw new RuntimeException("Sem credencial do CF Obras para " + estado
                    + " (configure CF_LOGIN_" + estado + " e CF_SENHA_" + estado + ")");
        }

        pagina.login(driver, wait, login, senha);
        modal.fecharModal(driver);
        Thread.sleep(1000);
        prepararTela(pagina, modal, driver, wait);
    }

    private static void prepararTela(CadastroPage pagina, ModalClose modal, WebDriver driver,
                                     WebDriverWait wait) throws Exception {
        pagina.abrirTelaEmpresa(driver);
        modal.fecharModal(driver);
        Thread.sleep(1000);
        pagina.clicarEditarOuPedirAjuste(driver, wait);
        Thread.sleep(1000);
        pagina.abrirAbaUsuarios(driver, wait);
        Thread.sleep(1000);
    }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}