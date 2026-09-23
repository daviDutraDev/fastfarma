import { apiGet, apiPut } from "./httpClient.js";

/**
 * Busca o perfil do usuario autenticado.
 * Backend: GET /api/auth/me
 */
export const BuscarMeuPerfil = async () => {
    return apiGet("/api/auth/me");
};

/**
 * Atualiza dados do proprio perfil (nome e telefone).
 * Backend: PUT /api/auth/me
 *
 * <p>A senha tem endpoint separado (PUT /api/auth/me/senha) para
 * exigir validacao da senha atual antes de trocar.</p>
 */
export const AtualizarMeuPerfil = async ({ nome, telefone }) => {
    return apiPut("/api/auth/me", { nome, telefone });
};

/**
 * Troca a senha do usuario autenticado.
 * Backend: PUT /api/auth/me/senha
 */
export const TrocarMinhaSenha = async (senhaAtual, novaSenha) => {
    return apiPut("/api/auth/me/senha", { senhaAtual, novaSenha });
};
