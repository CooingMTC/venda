package com.goti.venda.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.venda.dto.VendaDTOs.RequisicaoCompraDTO;
import com.goti.venda.model.Pedido;
import com.goti.venda.service.VendaService;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping("/comprar")
    public ResponseEntity<Map<String, String>> comprar(@RequestBody RequisicaoCompraDTO dto) {
        String protocolo = vendaService.iniciarCompra(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "PROCESSANDO",
                "mensagem", "Pedido registrado! Verificando disponibilidade de vagas...",
                "protocolo", protocolo
        ));
    }

    @GetMapping("/pedidos/{protocolo}")
    public ResponseEntity<Pedido> consultar(@PathVariable String protocolo) {
        return vendaService.buscarPorProtocolo(protocolo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}