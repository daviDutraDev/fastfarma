package com.fastfarma.controller;

import com.fastfarma.dto.*;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    // =============================================
    // GET /api/pedidos
    // Lista todos os pedidos (ordenados por ID desc)
    // =============================================
    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Lista de pedidos",
                pedidoService.listarTodos()));
    }

    // =============================================
    // GET /api/pedidos/cliente/{nome}
    // Lista pedidos de um cliente específico
    // =============================================
    @GetMapping("/cliente/{nome}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorCliente(
            @PathVariable String nome) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Pedidos do cliente",
                pedidoService.listarPorUsuario(nome)));
    }

    // =============================================
    // GET /api/pedidos/status/{status}
    // Lista pedidos por status
    // Status válidos: PENDENTE, APROVADO, REJEITADO, PRONTO
    // =============================================
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorStatus(
            @PathVariable String status) {
        try {
            StatusPedido s = StatusPedido.valueOf(status.toUpperCase());
            return ResponseEntity.ok(ApiResponse.ok(
                    "Pedidos com status " + s,
                    pedidoService.listarPorStatus(s)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro("Status inválido. Use: PENDENTE, APROVADO, REJEITADO ou PRONTO"));
        }
    }

    // =============================================
    // GET /api/pedidos/{id}
    // Retorna um pedido específico
    // =============================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok(
                    "Pedido encontrado",
                    pedidoService.buscarPorId(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    // =============================================
    // POST /api/pedidos
    // Cria um novo pedido
    // =============================================
    // Body (JSON):
    // {
    //   "idsProdutos": [1, 2, 3]
    // }
    // =============================================
    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> criar(
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeCliente,
            @Valid @RequestBody PedidoRequest request) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro("Header X-Usuario-Nome é obrigatório"));
        }
        try {
            return ResponseEntity.status(201)
                    .body(ApiResponse.ok(
                            "Pedido criado com sucesso",
                            pedidoService.criar(nomeCliente, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }

    // =============================================
    // PATCH /api/pedidos/{id}/status
    // Atualiza o status de um pedido
    // =============================================
    // Body (JSON):
    // {
    //   "status": "APROVADO"
    // }
    // Status válidos: PENDENTE, APROVADO, REJEITADO, PRONTO
    // =============================================
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PedidoResponse>> atualizarStatus(
            @PathVariable Integer id,
            @RequestBody StatusRequest request) {
        try {
            StatusPedido s = StatusPedido.valueOf(request.getStatus().toUpperCase());
            return ResponseEntity.ok(ApiResponse.ok(
                    "Status atualizado para " + s,
                    pedidoService.atualizarStatus(id, s)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro("Status inválido. Use: PENDENTE, APROVADO, REJEITADO ou PRONTO"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.erro(e.getMessage()));
        }
    }
}
