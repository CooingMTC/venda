# 🛒 Microsserviço de Venda (Order & Sales Service)

Microsserviço responsável por processar as compras de ingressos para transmissões ao vivo, orquestrando o fluxo assíncrono via mensageria com o RabbitMQ.

---

## 📌 Papel na Arquitetura

- **Orquestração Assíncrona:** Recebe os pedidos de compra, gera um protocolo UUID único e responde instantaneamente com `HTTP 202 Accepted` e status `PROCESSANDO`.
- **Comunicação por Filas:** Despacha uma mensagem para a fila de validação do serviço de produtos e aguarda o retorno assíncrono para atualizar o status do pedido.
- **Persistência de Pedidos:** Registra as transações em banco de dados H2 com os status:
  - `PROCESSANDO`
  - `COMPRA_REALIZADA`
  - `COMPRA_RECUSADA`

---

## ⚙️ Pré-requisitos & Dependências

- **Java 21**
- **RabbitMQ:** Necessário para o envio e recebimento de mensagens entre os serviços.
- **Relacionamento com outros serviços do ecossistema:**
  - **Requer o [produto-service](https://github.com/CooingMTC/produto.git) em execução:** O serviço de venda envia o comando de abatimento de vagas para ele. Sem o `produto`, o pedido permanecerá no estado `PROCESSANDO` aguardando consumo na fila.
  - **Relaciona-se com o [login-service](https://github.com/CooingMTC/login.git):** O `usuarioId` utilizado no momento da compra é obtido a partir do cadastro/login gerado no serviço de autenticação.

---

## 🚀 Como Executar

1. Navegue até a pasta do projeto:
   ```bash
   cd venda
   ```
2. Compile e execute a aplicação:
   ```bash
   ./mvnw clean compile
   ./mvnw spring-boot:run
   ```
3. A aplicação subirá na porta **`8081`**.

---

## 📡 Endpoints REST

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/vendas/comprar` | Inicia o processo de compra assíncrono (retorna protocolo) |
| `GET` | `/vendas/pedidos/{protocolo}` | Consulta o status consolidado do pedido |

---

### Exemplo de Requisição de Compra (`POST /vendas/comprar`):

```json
{
  "liveId": "bcb752d7-f8fb-4d7d-911a-bdac3832f821",
  "usuarioId": "f7d296c0-9f17-4ef6-829d-47209706782d",
  "quantidade": 2
}
```

**Resposta Imediata (`HTTP 202 Accepted`):**
```json
{
  "status": "PROCESSANDO",
  "mensagem": "Pedido registrado! Verificando disponibilidade de vagas...",
  "protocolo": "f3a18a99-b133-4f24-9b21-424b9148d5d1"
}
```

---

### Exemplo de Consulta (`GET /vendas/pedidos/{protocolo}`):

**Status 200 OK (Compra com sucesso):**
```json
{
  "protocolo": "f3a18a99-b133-4f24-9b21-424b9148d5d1",
  "liveId": "bcb752d7-f8fb-4d7d-911a-bdac3832f821",
  "usuarioId": "f7d296c0-9f17-4ef6-829d-47209706782d",
  "quantidade": 2,
  "status": "COMPRA_REALIZADA",
  "mensagem": "Compra realizada com sucesso! Vagas garantidas."
}
```

**Status 200 OK (Compra recusada por falta de vagas):**
```json
{
  "protocolo": "a823b123-c987-4321-bb12-9876543210fe",
  "liveId": "bcb752d7-f8fb-4d7d-911a-bdac3832f821",
  "usuarioId": "usuario-teste-2",
  "quantidade": 2,
  "status": "COMPRA_RECUSADA",
  "mensagem": "compra recusada, produto sem estoque / sala de transmissão lotada."
}
```

---

## 🔄 Filas do RabbitMQ Utilizadas

- **Publica em:** `venda.exchange` com routing key `venda.comando.solicitar` (Envia o pedido para o microsserviço `produto`).
- **Consome:** `venda.resposta.fila` (Recebe o resultado da verificação de vagas para finalizar o pedido).
