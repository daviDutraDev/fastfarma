import { apiDelete, apiGet } from "./httpClient.js";

/**
 * Lista todos os usuários cadastrados.
 * Backend: GET /api/usuarios
 */
export const BuscarUsuarios = async () => {
  return apiGet("/api/usuarios");
};

/**
 * Busca um usuário pelo id.
 * Backend: GET /api/usuarios/{id}
 */
export const BuscarUsuarioPorId = async (id) => {
  return apiGet(`/api/usuarios/${id}`);
};

/**
 * Exclui um usuário pelo id.
 * Backend: DELETE /api/usuarios/{id}
 * O backend protege o admin padrão (id=1) e responde 400 nesse caso.
 */
export const ExcluirUsuario = async (id) => {
  return apiDelete(`/api/usuarios/${id}`);
};
