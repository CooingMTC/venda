package com.goti.venda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.venda.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, String> {
}