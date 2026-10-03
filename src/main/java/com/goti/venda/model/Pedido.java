package com.goti.venda.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    private String protocolo;

    private String liveId;
    private String usuarioId;
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private String mensagem;
    private LocalDateTime dataCriacao;

    public enum StatusPedido {
        PROCESSANDO,
        COMPRA_REALIZADA,
        COMPRA_RECUSADA
    }

    public Pedido() {}

    public Pedido(String protocolo, String liveId, String usuarioId, Integer quantidade) {
        this.protocolo = protocolo;
        this.liveId = liveId;
        this.usuarioId = usuarioId;
        this.quantidade = quantidade;
        this.status = StatusPedido.PROCESSANDO;
        this.mensagem = "Pedido recebido, aguardando validação de vagas.";
        this.dataCriacao = LocalDateTime.now();
    }

    // Getters e Setters
    public String getProtocolo() { return protocolo; }
    public String getLiveId() { return liveId; }
    public String getUsuarioId() { return usuarioId; }
    public Integer getQuantidade() { return quantidade; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}