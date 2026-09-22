import { apiPost } from "./httpClient.js";

/**
 * Realiza login. Devolve a resposta completa do backend — o AuthContext
 * extrai o token de `dados.token`.
 * Backend: POST /api/auth/login
 */
export const FazerLogin = async (email, senha) => {
  return apiPost("/api/auth/login", { email, senha });
};
