import { apiDelete, apiGet } from "./httpClient.js";

/**
 * Lista todos os usuarios cadastrados (admin). Requer role FUNCIONARIO.
 * Backend: GET /api/usuarios
 */
export const BuscarUsuarios = async () => {
  return apiGet("/api/usuarios");
};

/**
 * Busca um usuario pelo id (admin). Requer role FUNCIONARIO.
 * Backend: GET /api/usuarios/{id}
 */
export const BuscarUsuarioPorId = async (id) => {
  return apiGet(`/api/usuarios/${id}`);
};

/**
 * Exclui um usuario pelo id (admin). Requer role FUNCIONARIO.
 * Backend: DELETE /api/usuarios/{id}
 * O backend protege o admin padrao (id=1).
 */
export const ExcluirUsuario = async (id) => {
  return apiDelete(`/api/usuarios/${id}`);
};
