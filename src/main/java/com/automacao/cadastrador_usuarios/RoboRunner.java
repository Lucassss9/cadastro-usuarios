package com.automacao.cadastrador_usuarios.app;

import com.automacao.cadastrador_usuarios.page.CadastroPage;
import com.automacao.cadastrador_usuarios.ui.Dialogs;
import com.automacao.cadastrador_usuarios.util.ApiClient;
import com.automacao.cadastrador_usuarios.util.DriverFactory;
import com.automacao.cadastrador_usuarios.util.ModalClose;
import com.automacao.cadastrador_usuarios.util.PerfilResolver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
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

    private static final boolean MODO_TESTE = !"false".equalsIgnoreCase(env("MODO_TESTE", "true"));

    public static void main(String[] args) {
        if (ADMIN_EMAIL.isBlank() || ADMIN_SENHA.isBlank()) {
            System.out.println("Configure CFOBRAS_ADMIN_EMAIL e CFOBRAS_ADMIN_SENHA antes de rodar.");
            return;
        }

        ApiClient api = new ApiClient(URL_BACKEND);
        List<Map<String, String>> fila;

        try {
            api.login(ADMIN_EMAIL, ADMIN_SENHA);
            fila = api.buscarPendentes();
        } catch (Exception e) {
            System.out.println("Nao consegui falar com o backend: " + e.getMessage());
            return;
        }

        if (fila.isEmpty()) {
            System.out.println("Nada na fila. Nenhum cadastro aprovado esperando.");
            return;
        }

        System.out.println(fila.size() + " cadastro(s) na fila."
                + (MODO_TESTE ? "  [MODO TESTE: nao vai salvar]" : "  [VALENDO: vai salvar de verdade]"));

        WebDriver driver = new DriverFactory().createChrome();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
        CadastroPage pagina = new CadastroPage(new Dialogs());
        ModalClose modal = new ModalClose();

        String estadoLogado = null;
        int ok = 0;
        int falhou = 0;

        try {
            for (Map<String, String> item : fila) {
                String id = item.get("id");
                String nome = item.get("nome");
                System.out.println("");
                System.out.println("--- " + nome + " (id " + id + ")");
                if (item.get("obras_todas") != null && !item.get("obras_todas").isBlank())
                    System.out.println("    obras: " + item.get("obras_todas"));
                if (item.get("observacao") != null && !item.get("observacao").isBlank())
                    System.out.println("    obs: " + item.get("observacao"));

                try {
                    if ("true".equalsIgnoreCase(item.get("ja_tem_acesso"))) {
                        System.out.println("  PULADO: já tem acesso, é só vincular (manual).");
                        api.atualizarStatus(id, "cadastrado", null);
                        falhou++;
                        continue;
                    }

                    String perfil = PerfilResolver.resolver(
                            item.get("funcao"),
                            "true".equalsIgnoreCase(item.get("terceirizado")));

                    if (perfil == null) {
                        String msg = "Sem regra de permissao para o cargo '" + item.get("funcao") + "'";
                        System.out.println("  PULADO: " + msg);
                        api.atualizarStatus(id, "erro", msg);
                        falhou++;
                        continue;
                    }

                    api.atualizarStatus(id, "processando", null);

                    String estado = item.get("estado");
                    if (!estado.equals(estadoLogado)) {
                        System.out.println("  Entrando no CF Obras como " + estado + "...");
                        entrar(pagina, modal, driver, wait, estado);
                        estadoLogado = estado;
                    }

                    pagina.garantirAbaUsuarios(driver);
                    pagina.selecionarObra(driver, wait, item.get("obra"));

                    item.put("perfil", perfil);
                    pagina.preencherCamposBasicos(driver, wait, item);
                    pagina.selecionarPerfil(driver, perfil);
                    pagina.marcarPermissoes(driver, perfil);

                    if (MODO_TESTE) {
                        System.out.println("  Preenchido (nao salvo). Confira na tela.");
                        Thread.sleep(4000);
                        api.atualizarStatus(id, "erro", "Modo teste: preenchido mas nao salvo");
                        falhou++;
                        continue;
                    }

                    pagina.clicarSalvar(driver, wait);
                    Thread.sleep(2000);

                    String erroNaTela = pagina.verificarErroNaTela(driver);
                    if (erroNaTela != null) {
                        System.out.println("  ERRO do site: " + erroNaTela);
                        api.atualizarStatus(id, "erro", erroNaTela);
                        falhou++;
                    } else {
                        System.out.println("  Cadastrado.");
                        api.atualizarStatus(id, "cadastrado", null);
                        ok++;
                    }

                    driver.navigate().refresh();
                    Thread.sleep(2000);
                    prepararTela(pagina, modal, driver, wait);

                } catch (Exception e) {
                    System.out.println("  FALHOU: " + e);
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
            System.out.println("");
            System.out.println("===== fim: " + ok + " ok, " + falhou + " com problema =====");
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