# Cadastro de Usuários (Automação)

Robô em Java que automatiza o cadastro de funcionários, puxando dados diretamente de um sistema de central de ajuda interno que eu também desenvolvi.

## 🎯 Problema que resolve

Solicitações de cadastro de funcionário chegam pela central de ajuda e passam por um fluxo de aprovação. Depois de aprovadas, alguém ainda precisa entrar no sistema de gestão e cadastrar manualmente cada funcionário com os dados da solicitação — um passo repetitivo que só existe porque os dois sistemas não conversam entre si.

Esse robô fecha essa lacuna: identifica o que já foi aprovado e cadastra automaticamente.

## ⚙️ Como funciona

```
Central de Ajuda (banco de dados) ──► Robô ──► Sistema de gestão
                                        │
                                        ├── Consulta solicitações de cadastro
                                        ├── Filtra as que estão com status "aprovado"
                                        └── Executa o cadastro de cada uma
```

1. **Consulta ao banco** — acessa o banco de dados da central de ajuda (sistema próprio) e busca as solicitações de cadastro de funcionário.
2. **Filtro por status** — identifica quais solicitações já foram aprovadas no fluxo de aprovação.
3. **Cadastro automático** — para cada uma aprovada, executa o cadastro do funcionário no sistema de gestão, usando os dados da solicitação.

## 🛠️ Tech Stack

- **Java** — linguagem principal
- **Maven** — gerenciamento de dependências e build
- **Banco de dados** — consulta às solicitações e seus status

## ▶️ Execução

O robô é executado manualmente — não roda em background nem é agendado. Cada execução processa as solicitações aprovadas até aquele momento.

## 📌 Contexto

Esse robô se conecta a outro sistema que desenvolvi (a central de ajuda), fechando o ciclo entre "solicitação aprovada" e "funcionário cadastrado" sem intervenção manual repetitiva. Projetado e implementado sozinho.
