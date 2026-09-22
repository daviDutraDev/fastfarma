import { apiPut } from "./httpClient.js";

/**
 * Adiciona unidades ao estoque de um produto.
 * Backend: PUT /api/estoque/adicionar/{id}
 * Requer role FUNCIONARIO.
 */
export const atualizarEstoque = async (id, quantidade) => {
  return apiPut(`/api/estoque/adicionar/${id}`, { quantidade });
};
