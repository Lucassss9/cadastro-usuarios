package com.automacao.cadastrador_usuarios.app;

import com.automacao.cadastrador_usuarios.page.CadastroPage;
import com.automacao.cadastrador_usuarios.page.VinculoPage;
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
import java.util.LinkedHashMap;

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
        List<Map<String, String>> jaCadastrados;

        try {
            api.login(ADMIN_EMAIL, ADMIN_SENHA);
            fila = api.buscarPendentes();
            jaCadastrados = api.buscarParaVincular();
        } catch (Exception e) {
            dialogs.info("Nao consegui falar com o backend: " + e.getMessage());
            return;
        }

        if (fila.isEmpty() && jaCadastrados.isEmpty()) {
            dialogs.info("Nada na fila. Nenhum cadastro aprovado nem ninguem para vincular.");
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
        List<Map<String, String>> paraVincular = new ArrayList<>();
        paraVincular.addAll(jaCadastrados);
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
                        paraVincular.add(item);
                        relatorio.add(item);
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

                    try {
                        api.atualizarStatus(id, "processando", null);
                    } catch (Exception ignorado) {
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
                            item.put("status_cadastro", "JA EXISTE (falta vincular na mao)");
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
                                paraVincular.add(item);
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
                            paraVincular.add(item);
                            api.atualizarStatus(id, "cadastrado", null);
                            ok++;
                        }
                    } else if (acao == 1) {
                        item.put("status_cadastro", "SALVO MANUALMENTE");
                        paraVincular.add(item);
                        api.atualizarStatus(id, "cadastrado", null);
                        ok++;
                    } else {
                        item.put("status_cadastro", "NAO SALVO");
                        api.atualizarStatus(id, "erro", "Conferido mas nao salvo pelo admin");
                        falhou++;
                    }

                    relatorio.add(item);
                    pagina.garantirAbaUsuarios(driver);
                    Thread.sleep(500);

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
            if (!paraVincular.isEmpty()) {
                status.atualizar("Iniciando vinculos em obras...");
                vincularEmObras(paraVincular, pagina, modal, driver, wait, status, api, estadoLogado);
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

    private static void entrarSoLogin(CadastroPage pagina, ModalClose modal, WebDriver driver,
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


    private static void vincularEmObras(List<Map<String, String>> paraVincular,
                                        CadastroPage pagina, ModalClose modal,
                                        WebDriver driver, WebDriverWait wait,
                                        JanelaStatus status, ApiClient api,
                                        String estadoLogado) {
        VinculoPage vinculo = new VinculoPage();
        Map<String, String> problemasDaPessoa = new LinkedHashMap<>();

        // Agrupa por estado + codigo de filial. Uma pessoa pode ter varias obras/filiais.
        Map<String, List<Map<String, String>>> porFilial = new LinkedHashMap<>();
        Map<String, String> estadoDaChave = new LinkedHashMap<>();

        for (Map<String, String> pessoa : paraVincular) {
            String estado = pessoa.get("estado");
            String todas = pessoa.get("obras_todas");
            if (todas == null || todas.isBlank()) todas = pessoa.get("obra");
            if (todas == null || todas.isBlank()) continue;

            java.util.Set<String> codigosDaPessoa = new java.util.LinkedHashSet<>();
            for (String uma : todas.split(" ; ")) {
                String cod = codigoFilialDe(uma);
                if (!cod.isBlank()) codigosDaPessoa.add(cod);
            }
            for (String cod : codigosDaPessoa) {
                String chave = estado + "|" + cod;
                porFilial.computeIfAbsent(chave, k -> new ArrayList<>()).add(pessoa);
                estadoDaChave.put(chave, estado);
            }
        }

        for (Map.Entry<String, List<Map<String, String>>> grupo : porFilial.entrySet()) {
            String chave = grupo.getKey();
            String estado = estadoDaChave.get(chave);
            String codigoFilial = chave.substring(chave.indexOf('|') + 1);
            List<Map<String, String>> pessoas = grupo.getValue();

            try {
                if (!estado.equals(estadoLogado)) {
                    status.atualizar("Entrando no CF Obras como " + estado + " (vinculos)...");
                    entrarSoLogin(pagina, modal, driver, wait, estado);
                    estadoLogado = estado;
                }

                status.atualizar("Filial " + codigoFilial + ": selecionando...");
                boolean okFilial = vinculo.selecionarFilial(driver, wait, codigoFilial);
                if (!okFilial) {
                    System.out.println("Pulei filial " + codigoFilial + ": nao consegui selecionar.");
                    continue;
                }

                vinculo.abrirTelaObras(driver, wait);
                List<String> obras = vinculo.listarObrasDaTela(driver);
                System.out.println("== FILIAL " + codigoFilial + " | obras encontradas ("
                        + obras.size() + "): " + obras);
                if (obras.isEmpty()) {
                    System.out.println("Filial " + codigoFilial + ": nenhuma obra listada.");
                    continue;
                }

                for (String obra : obras) {
                    List<Map<String, String>> alvo = new ArrayList<>();
                    for (Map<String, String> pessoa : pessoas) {
                        String setor = pessoa.get("setor");
                        if (vinculo.setorBateComObra(setor, obra)) alvo.add(pessoa);
                    }
                    if (alvo.isEmpty()) continue;

                    status.atualizar("Obra " + obra + ": vinculando " + alvo.size() + " pessoa(s)...");
                    System.out.println(">> OBRA: " + obra + " | vou tentar vincular: "
                            + alvo.stream().map(pp -> pp.get("nome")).toList());
                    if (!vinculo.abrirEdicaoDaObra(driver, wait, obra)) {
                        System.out.println("   NAO consegui abrir a edicao da obra: " + obra);
                        for (Map<String, String> pessoa : alvo) {
                            problemasDaPessoa.merge(pessoa.get("id"),
                                    "Nao abriu a obra " + obra, (a, b) -> a + " | " + b);
                        }
                        continue;
                    }
                    try {
                        vinculo.abrirAbaUsuarios(driver, wait);
                    } catch (Exception e) {
                        System.out.println("   NAO abri aba Usuarios da obra: " + obra);
                        vinculo.voltarParaLista(driver, wait);
                        continue;
                    }

                    System.out.println("   Disponiveis na obra: " + vinculo.nomesNaColuna(driver, "Usuário disponível"));
                    System.out.println("   Ja vinculados na obra: " + vinculo.nomesNaColuna(driver, "Usuário vinculado"));

                    boolean mudouAlgo = false;
                    for (Map<String, String> pessoa : alvo) {
                        String nome = pessoa.get("nome");
                        String resultado = vinculo.vincularPessoa(driver, wait, nome);
                        if ("ok".equals(resultado)) {
                            mudouAlgo = true;
                        } else if ("ja".equals(resultado)) {
                            // ja estava vinculado, nada a fazer
                        } else if ("nao_achei".equals(resultado)) {
                            String falha = "Nao achou '" + nome + "' em disponivel na obra " + obra;
                            System.out.println("   " + falha);
                            problemasDaPessoa.merge(pessoa.get("id"), falha, (a, b) -> a + " | " + b);
                        } else {
                            String falha = "Falhou vincular '" + nome + "' na obra " + obra;
                            System.out.println("   " + falha);
                            problemasDaPessoa.merge(pessoa.get("id"), falha, (a, b) -> a + " | " + b);
                        }
                    }

                    if (mudouAlgo) {
                        System.out.println("   Salvando a obra " + obra + "...");
                        vinculo.salvarObra(driver, wait);
                    } else {
                        System.out.println("   Nada mudou na obra " + obra + " (ninguem novo vinculado).");
                    }
                    vinculo.voltarParaLista(driver, wait);
                }

                for (Map<String, String> pessoa : pessoas) {
                    String idPessoa = pessoa.get("id");
                    String problema = problemasDaPessoa.get(idPessoa);
                    try {
                        if (problema == null) {
                            String observacao = null;
                            boolean jaTinhaAcesso = "true".equalsIgnoreCase(pessoa.get("ja_tem_acesso"));
                            String senha = pessoa.get("senha_padrao");
                            if (!jaTinhaAcesso && senha != null && !senha.isBlank()) {
                                observacao = "Senha inicial: " + senha;
                            }
                            api.atualizarStatus(idPessoa, "vinculado", observacao);
                        } else {
                            api.atualizarStatus(idPessoa, "cadastrado",
                                    "Vinculo incompleto: " + problema);
                        }
                    } catch (Exception ignorado) {
                    }
                }

            } catch (Exception e) {
                System.out.println("Erro no vinculo da filial " + codigoFilial + ": " + e.getMessage());
            }
        }
    }

    private static String codigoFilialDe(String textoObra) {
        if (textoObra == null || textoObra.isBlank()) return "";
        String t = textoObra.trim();
        int traco = t.indexOf('-');
        String cod = (traco > 0 ? t.substring(0, traco) : t.split("\\s+")[0]).trim();
        return cod;
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