package com.fastfarma.controller;

import com.fastfarma.dto.ApiResponse;
import com.fastfarma.dto.PedidoRequest;
import com.fastfarma.dto.PedidoResponse;
import com.fastfarma.dto.StatusRequest;
import com.fastfarma.model.StatusPedido;
import com.fastfarma.repository.PedidoRepository;
import com.fastfarma.security.AuthPrincipal;
import com.fastfarma.service.IPedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final IPedidoService pedidoService;
    private final PedidoRepository pedidoRepository;

    private boolean isFuncionario(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_FUNCIONARIO"::equals);
    }

    private void exigirAcessoOuProprio(Authentication auth, String criadoPor) {
        if (isFuncionario(auth)) return;
        String atual = AuthPrincipal.currentName();
        if (atual == null || !atual.equalsIgnoreCase(criadoPor)) {
            throw new AccessDeniedException(
                    "Você só pode acessar pedidos criados por você mesmo.");
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Lista de pedidos", pedidoService.listarTodos()));
    }

    @GetMapping("/cliente/{nome}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorCliente(
            @PathVariable String nome, Authentication auth) {
        if (!isFuncionario(auth)) {
            String atual = AuthPrincipal.currentName();
            if (atual == null || !atual.equalsIgnoreCase(nome)) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.erro("Você só pode ver seus próprios pedidos."));
            }
        }
        return ResponseEntity.ok(ApiResponse.ok("Pedidos do cliente", pedidoService.listarPorUsuario(nome)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorStatus(@PathVariable String status) {
        try {
            StatusPedido s = StatusPedido.valueOf(status.toUpperCase());
            return ResponseEntity.ok(ApiResponse.ok("Pedidos com status " + s, pedidoService.listarPorStatus(s)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro("Status inválido. Use: PENDENTE, APROVADO, REJEITADO ou PRONTO"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> buscarPorId(
            @PathVariable Integer id, Authentication auth) {
        try {
            var pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
            exigirAcessoOuProprio(auth, pedido.getCriadoPor());
            return ResponseEntity.ok(ApiResponse.ok("Pedido encontrado", pedidoService.buscarPorId(id)));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(ApiResponse.erro(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.erro(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> criar(
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeCliente,
            @Valid @RequestBody PedidoRequest request) {
        try {
            return ResponseEntity.status(201)
                    .body(ApiResponse.ok("Pedido criado com sucesso", pedidoService.criar(nomeCliente, request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.erro(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PedidoResponse>> atualizarStatus(
            @PathVariable Integer id, @RequestBody StatusRequest request) {
        try {
            StatusPedido s = StatusPedido.valueOf(request.getStatus().toUpperCase());
            return ResponseEntity.ok(ApiResponse.ok("Status atualizado para " + s,
                    pedidoService.atualizarStatus(id, s)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.erro("Status inválido. Use: PENDENTE, APROVADO, REJEITADO ou PRONTO"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.erro(e.getMessage()));
        }
    }
}
