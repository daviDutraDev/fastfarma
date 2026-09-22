import { API_BASE_URL, API_TIMEOUT_MS } from "../../config/api.js";

/**
 * Erro de domínio da API.
 *
 * Carrega a mensagem vinda do backend (`ApiResponse.mensagem`) ou
 * um fallback. Preserva o status HTTP e a resposta completa para que
 * a UI possa reagir (ex.: logout em 401).
 */
export class ApiError extends Error {
  constructor(message, { status, payload, cause } = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status ?? null;
    this.payload = payload ?? null;
    if (cause) this.cause = cause;
  }
}

/** Callback registrado pelo AuthContext para reagir a 401. */
let onUnauthorized = null;
export const setOnUnauthorized = (fn) => { onUnauthorized = fn; };

const readToken = () => {
  try {
    const raw = localStorage.getItem("fastfarma:auth");
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (parsed?.expiresAt && parsed.expiresAt < Date.now()) return null;
    return parsed?.token ?? null;
  } catch { return null; }
};

const buildHeaders = (extra, hasBody) => {
  const headers = { Accept: "application/json", ...extra };
  if (hasBody && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }
  const token = readToken();
  if (token && !headers.Authorization) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
};

const withTimeout = (ms, init) => {
  if (!ms) return init;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), ms);
  return { ...init, signal: controller.signal, __clear: () => clearTimeout(timer) };
};

const parseBody = async (res) => {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
};

const extractMessage = (payload, fallback) => {
  if (!payload) return fallback;
  if (typeof payload === "string") return payload;
  if (typeof payload.mensagem === "string") return payload.mensagem;
  if (typeof payload.message === "string") return payload.message;
  if (typeof payload.error === "string") return payload.error;
  return fallback;
};

/**
 * Cliente HTTP único para a FastFarma API.
 *
 * Constrói URLs a partir de `API_BASE_URL`, normaliza headers,
 * injeta o Bearer token automaticamente, dispara timeout e converte
 * respostas não-2xx em `ApiError` carregando a mensagem do backend.
 *
 * @param {string} path     Caminho começando com `/` (ex.: `/api/auth/login`).
 * @param {object} options  { method, body, headers, timeoutMs, raw }
 *                          `raw: true` retorna `Response` sem tratamento.
 */
export const apiRequest = async (path, options = {}) => {
  const { method = "GET", body, headers = {}, timeoutMs = API_TIMEOUT_MS, raw = false } = options;

  const url = `${API_BASE_URL}${path}`;
  const hasBody = body !== undefined && body !== null;
  const init = withTimeout(timeoutMs, {
    method,
    headers: buildHeaders(headers, hasBody),
    body: hasBody ? JSON.stringify(body) : undefined,
  });

  let res;
  try {
    res = await fetch(url, init);
  } catch (networkError) {
    init.__clear?.();
    throw new ApiError(
      `Não foi possível conectar ao servidor (${API_BASE_URL}). Verifique se a API está em execução.`,
      { cause: networkError }
    );
  } finally {
    init.__clear?.();
  }

  if (raw) return res;

  const payload = await parseBody(res);

  if (res.status === 401 && onUnauthorized) {
    try { onUnauthorized(); } catch { /* swallow */ }
  }

  if (!res.ok) {
    throw new ApiError(extractMessage(payload, `Erro ${res.status} na requisição`), {
      status: res.status,
      payload,
    });
  }

  return payload;
};

/** Helpers para verbos HTTP. */
export const apiGet    = (path, opts)         => apiRequest(path, { ...opts, method: "GET" });
export const apiPost   = (path, body, opts)   => apiRequest(path, { ...opts, method: "POST", body });
export const apiPut    = (path, body, opts)   => apiRequest(path, { ...opts, method: "PUT", body });
export const apiPatch  = (path, body, opts)   => apiRequest(path, { ...opts, method: "PATCH", body });
export const apiDelete = (path, opts)         => apiRequest(path, { ...opts, method: "DELETE" });
