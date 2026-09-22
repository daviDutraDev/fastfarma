import { apiPost } from "./httpClient.js";

/**
 * Cadastra novo cliente.
 * Backend: POST /api/auth/cadastrar
 */
export const CadastrarUsuario = async (nome, email, senha, telefone) => {
  return apiPost("/api/auth/cadastrar", {
    nome,
    email,
    senha,
    telefone: telefone || null,
  });
};
