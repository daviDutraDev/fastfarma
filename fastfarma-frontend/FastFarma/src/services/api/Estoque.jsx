import { apiPut } from "./httpClient.js";

/**
 * Adiciona unidades ao estoque de um produto.
 * Backend: PUT /api/estoque/adicionar/{id}
 * Payload: { quantidade: number }
 */
export const AdicionarEstoque = async (id, quantidade) => {
  return apiPut(`/api/estoque/adicionar/${id}`, { quantidade });
};
