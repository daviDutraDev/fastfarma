package com.fastfarma.service;

import com.fastfarma.dto.PedidoRequest;
import com.fastfarma.dto.PedidoResponse;
import com.fastfarma.model.StatusPedido;

import java.util.List;

/**
 * Contrato do serviço de pedidos.
 * Aplicado o Princípio da Inversão de Dependência.
 */
public interface IPedidoService {

    List<PedidoResponse> listarTodos();

    List<PedidoResponse> listarPorUsuario(String nome);

    List<PedidoResponse> listarPorStatus(StatusPedido status);

    PedidoResponse buscarPorId(Integer id);

    PedidoResponse criar(String nomeCliente, PedidoRequest request);

    PedidoResponse atualizarStatus(Integer id, StatusPedido novoStatus);
}
