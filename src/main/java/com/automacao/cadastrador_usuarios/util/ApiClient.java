package com.automacao.cadastrador_usuarios.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private final String urlBase;
    private final HttpClient http;
    private final ObjectMapper json = new ObjectMapper();
    private String token;

    public ApiClient(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public void login(String email, String senha) throws Exception {
        String corpo = json.writeValueAsString(Map.of("email", email, "senha", senha));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/usuario/login"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("Login no backend falhou (" + res.statusCode() + "): " + res.body());
        }

        JsonNode dados = json.readTree(res.body());
        this.token = dados.get("token").asText();

        String papel = dados.path("papel").asText("");
        if (!"admin".equals(papel)) {
            throw new RuntimeException("Esta conta e '" + papel + "'. O robo precisa de uma conta admin.");
        }
    }

    public List<Map<String, String>> buscarPendentes() throws Exception {
        return buscarFila("/colaborador/pendentes");
    }

    public List<Map<String, String>> buscarParaVincular() throws Exception {
        return buscarFila("/colaborador/para-vincular");
    }

    private List<Map<String, String>> buscarFila(String caminho) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + caminho))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new RuntimeException("Nao consegui buscar a fila (" + res.statusCode() + "): " + res.body());
        }

        List<Map<String, String>> fila = new ArrayList<>();
        for (JsonNode item : json.readTree(res.body())) {
            Map<String, String> dados = new HashMap<>();
            dados.put("id", item.path("id").asText());
            dados.put("nome", texto(item, "nome"));
            dados.put("email", texto(item, "email"));
            dados.put("funcao", texto(item, "funcao"));
            dados.put("estado", texto(item, "estado"));
            java.util.List<String> obras = new ArrayList<>();
            for (JsonNode o : item.path("obras")) obras.add(o.asText());
            dados.put("obra", obras.isEmpty() ? texto(item, "obra") : obras.get(0));
            dados.put("obras_todas", String.join(" ; ", obras));
            dados.put("observacao", texto(item, "observacao"));
            dados.put("setor", texto(item, "setor"));
            dados.put("ja_tem_acesso", String.valueOf(item.path("ja_tem_acesso").asBoolean(false)));
            dados.put("perfil", texto(item, "perfil"));
            dados.put("senha_padrao", texto(item, "senha_padrao"));
            dados.put("cpf", texto(item, "cpf"));
            dados.put("terceirizado", String.valueOf(item.path("terceirizado").asBoolean(false)));
            fila.add(dados);
        }
        return fila;
    }

    public void atualizarStatus(String id, String status, String erro) throws Exception {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("status", status);
        corpo.put("erro", erro);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/colaborador/" + id + "/status"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(60))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo)))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            System.out.println("Aviso: nao consegui atualizar o status do id " + id
                    + " (" + res.statusCode() + "): " + res.body());
        }
    }

    private String texto(JsonNode no, String campo) {
        JsonNode valor = no.path(campo);
        return (valor.isNull() || valor.isMissingNode()) ? "" : valor.asText();
    }
}