import { apiPost } from "./httpClient.js";

/**
 * Cadastra um novo cliente.
 * Backend: POST /api/auth/cadastrar
 * Payload: { nome, email, senha }
 * Retorno: ApiResponse<UsuarioResponse>
 *
 * O backend sempre cria o usuário com tipo CLIENTE.
 */
export const CadastrarUsuario = async (nome, email, senha) => {
  return apiPost("/api/auth/cadastrar", { nome, email, senha });
};
