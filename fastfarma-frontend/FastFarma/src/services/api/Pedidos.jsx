import { apiGet, apiPatch, apiPost } from "./httpClient.js";

/**
 * Lista todos os pedidos (admin). Requer role FUNCIONARIO.
 * Backend: GET /api/pedidos
 */
export const BuscarPedidos = async () => {
  return apiGet("/api/pedidos");
};

/**
 * Lista pedidos de um cliente pelo nome.
 * Backend: GET /api/pedidos/cliente/{nome}
 * CLIENTE só pode ver os proprios pedidos; caso contrario 403.
 */
export const BuscarPedidosUsuario = async (nome) => {
  return apiGet(`/api/pedidos/cliente/${encodeURIComponent(nome)}`);
};

/**
 * Busca um pedido pelo id.
 * Backend: GET /api/pedidos/{id}
 */
export const BuscarPedidoPorId = async (id) => {
  return apiGet(`/api/pedidos/${id}`);
};

/**
 * Lista pedidos por status (admin). Requer role FUNCIONARIO.
 */
export const BuscarPedidosPorStatus = async (status) => {
  return apiGet(`/api/pedidos/status/${encodeURIComponent(status)}`);
};

/**
 * Cria um novo pedido (qualquer usuario autenticado).
 * Backend: POST /api/pedidos
 * O nome do cliente é extraído do JWT pelo backend.
 */
export const CriarPedido = async (idsProdutos) => {
  return apiPost("/api/pedidos", { idsProdutos });
};

/**
 * Atualiza o status de um pedido (admin). Requer role FUNCIONARIO.
 * Backend: PATCH /api/pedidos/{id}/status
 */
export const AtualizarStatusPedido = async (id, status) => {
  return apiPatch(`/api/pedidos/${id}/status`, { status });
};
