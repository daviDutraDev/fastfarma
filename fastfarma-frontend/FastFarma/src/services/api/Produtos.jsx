import { apiDelete, apiGet, apiPost, apiPut } from "./httpClient.js";

/**
 * Lista todos os produtos (autenticado).
 * Backend: GET /api/produtos
 */
export const BuscarProdutos = async () => {
  return apiGet("/api/produtos");
};

/**
 * Busca um produto pelo id.
 * Backend: GET /api/produtos/{id}
 */
export const BuscarProdutoPorId = async (id) => {
  return apiGet(`/api/produtos/${id}`);
};

/**
 * Lista produtos disponíveis (estoque > 0).
 * Backend: GET /api/produtos/disponiveis
 */
export const BuscarProdutosDisponiveis = async () => {
  return apiGet("/api/produtos/disponiveis");
};

/**
 * Lista produtos esgotados.
 * Backend: GET /api/produtos/esgotados
 */
export const BuscarProdutosEsgotados = async () => {
  return apiGet("/api/produtos/esgotados");
};

/**
 * Busca produtos pelo nome (case-insensitive, parcial).
 * Backend: GET /api/produtos/buscar?nome=...
 */
export const BuscarProdutosPorNome = async (nome) => {
  const query = encodeURIComponent(nome ?? "");
  return apiGet(`/api/produtos/buscar?nome=${query}`);
};

/**
 * Cria um novo produto. Requer role FUNCIONARIO.
 * Backend: POST /api/produtos
 */
export const cadastrarProduto = async ({ nome, preco, estoque, categoria }) => {
  return apiPost("/api/produtos", { nome, preco, estoque, categoria });
};

/**
 * Atualiza um produto existente. Requer role FUNCIONARIO.
 * Backend: PUT /api/produtos/{id}
 */
export const AtualizarProduto = async (id, { nome, preco, estoque, categoria }) => {
  return apiPut(`/api/produtos/${id}`, { nome, preco, estoque, categoria });
};

/**
 * Exclui um produto. Requer role FUNCIONARIO.
 * Backend: DELETE /api/produtos/{id}
 */
export const deletarProduto = async (id) => {
  return apiDelete(`/api/produtos/${id}`);
};
