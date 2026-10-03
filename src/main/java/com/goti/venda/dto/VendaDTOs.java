package com.goti.venda.dto;

import java.io.Serializable;

public class VendaDTOs {

    // Enviado pelo cliente via POST
    public record RequisicaoCompraDTO(String liveId, String usuarioId, Integer quantidade) {}

    // Mensagem enviada para a fila de solicitação
    public record MensagemSolicitacaoDTO(String protocolo, String liveId, Integer quantidade) implements Serializable {}

    // Mensagem recebida da fila de resposta do produto
    public record MensagemRespostaDTO(String protocolo, boolean aprovado, String motivo) implements Serializable {}
}