package com.goti.venda.service;


import com.goti.venda.config.RabbitMQConfig;
import com.goti.venda.dto.VendaDTOs.*;
import com.goti.venda.model.Pedido;
import com.goti.venda.model.Pedido.StatusPedido;
import com.goti.venda.repository.PedidoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class VendaService {

    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;

    public VendaService(PedidoRepository pedidoRepository, RabbitTemplate rabbitTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public String iniciarCompra(RequisicaoCompraDTO dto) {
        String protocolo = UUID.randomUUID().toString();

        // 1. Salva o pedido inicial no banco de vendas com status PROCESSANDO
        Pedido pedido = new Pedido(protocolo, dto.liveId(), dto.usuarioId(), dto.quantidade());
        pedidoRepository.save(pedido);

        // 2. Envia para o RabbitMQ para o serviço de PRODUTO validar
        MensagemSolicitacaoDTO msg = new MensagemSolicitacaoDTO(protocolo, dto.liveId(), dto.quantidade());
        rabbitTemplate.convertAndSend(RabbitMQConfig.VENDA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_SOLICITACAO, msg);

        return protocolo;
    }

    // 3. Ouve a resposta vinda do microsserviço de PRODUTO
    @RabbitListener(queues = RabbitMQConfig.FILA_RESPOSTA)
    @Transactional
    public void receberRespostaProduto(MensagemRespostaDTO resposta) {
        pedidoRepository.findById(resposta.protocolo()).ifPresent(pedido -> {
            if (resposta.aprovado()) {
                pedido.setStatus(StatusPedido.COMPRA_REALIZADA);
                pedido.setMensagem("Compra realizada com sucesso! Vagas garantidas.");
            } else {
                pedido.setStatus(StatusPedido.COMPRA_RECUSADA);
                pedido.setMensagem(resposta.motivo());
            }
            pedidoRepository.save(pedido);
        });
    }

    public Optional<Pedido> buscarPorProtocolo(String protocolo) {
        return pedidoRepository.findById(protocolo);
    }
}