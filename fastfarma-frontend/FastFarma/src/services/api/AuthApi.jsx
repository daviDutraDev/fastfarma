import { apiPost } from "./httpClient.js";

/**
 * Autentica o usuário.
 * Backend: POST /api/auth/login
 * Payload: { email, senha }
 * Retorno: ApiResponse<LoginResponse> = { sucesso, mensagem, dados: { id, nome, email, tipo, mensagem } }
 */
export const FazerLogin = async (email, senha) => {
  return apiPost("/api/auth/login", { email, senha });
};
