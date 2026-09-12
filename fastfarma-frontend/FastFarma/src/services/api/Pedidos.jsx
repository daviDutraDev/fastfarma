import { apiGet, apiPatch, apiPost } from "./httpClient.js";

/**
 * Lista todos os pedidos (ordenados por id desc no backend).
 * Backend: GET /api/pedidos
 */
export const BuscarPedidos = async () => {
  return apiGet("/api/pedidos");
};

/**
 * Busca um pedido pelo id.
 * Backend: GET /api/pedidos/{id}
 */
export const BuscarPedidoPorId = async (id) => {
  return apiGet(`/api/pedidos/${id}`);
};

/**
 * Lista pedidos filtrados por status.
 * Backend: GET /api/pedidos/status/{status}
 * @param {"PENDENTE"|"APROVADO"|"REJEITADO"|"PRONTO"} status
 */
export const BuscarPedidosPorStatus = async (status) => {
  return apiGet(`/api/pedidos/status/${encodeURIComponent(status)}`);
};

/**
 * Lista pedidos de um cliente pelo nome.
 * Backend: GET /api/pedidos/cliente/{nome}
 */
export const BuscarPedidosPorCliente = async (nome) => {
  return apiGet(`/api/pedidos/cliente/${encodeURIComponent(nome)}`);
};

/**
 * Cria um novo pedido.
 * Backend: POST /api/pedidos
 * Headers obrigatórios: X-Usuario-Nome (nome do cliente logado)
 * Payload: PedidoRequest { idsProdutos: number[] }
 */
export const CriarPedido = async (nomeCliente, idsProdutos) => {
  return apiPost(
    "/api/pedidos",
    { idsProdutos },
    { headers: { "X-Usuario-Nome": nomeCliente } }
  );
};

/**
 * Atualiza o status de um pedido.
 * Backend: PATCH /api/pedidos/{id}/status
 * Payload: { status: "PENDENTE"|"APROVADO"|"REJEITADO"|"PRONTO" }
 */
export const AtualizarStatusPedido = async (id, status) => {
  return apiPatch(`/api/pedidos/${id}/status`, { status });
};
