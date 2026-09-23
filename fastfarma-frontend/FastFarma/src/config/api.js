/**
 * Configuração central da API.
 *
 * Lê a URL base de `VITE_API_URL` (definida em `.env`) com fallback
 * para o endereço local do backend Spring Boot. Mantém o resto da
 * aplicação livre de URLs hardcoded — basta importar de `apiClient`.
 */
const RAW_BASE_URL =
  import.meta.env.VITE_API_URL?.trim() || "http://localhost:8080";

/** Remove barra final para evitar `//api/...` nas URLs montadas. */
export const API_BASE_URL = RAW_BASE_URL.replace(/\/+$/, "");

export const API_TIMEOUT_MS = Number(import.meta.env.VITE_API_TIMEOUT) || 15000;
