import { API_BASE_URL } from "../../config/api.js";

const AUTH_STORAGE_KEY = "fastfarma:auth";

const readToken = () => {
    try {
        const raw = localStorage.getItem(AUTH_STORAGE_KEY);
        if (!raw) return null;
        const parsed = JSON.parse(raw);
        if (parsed?.expiresAt && parsed.expiresAt < Date.now()) return null;
        return parsed?.token ?? null;
    } catch { return null; }
};

/**
 * Dispara o download de um relatorio em PDF.
 *
 * <p>O backend devolve application/pdf com Content-Disposition: attachment.
 * Aqui a gente chama via fetch + Authorization Bearer, recebe o blob
 * e cria um link temporario para o browser baixar.</p>
 *
 * @param {string} path Caminho do endpoint (ex.: "/api/relatorios/pedidos")
 * @param {string} filename Nome sugerido para o arquivo baixado
 */
const baixarPdf = async (path, filename) => {
    const token = readToken();
    const headers = { Accept: "application/pdf" };
    if (token) headers.Authorization = `Bearer ${token}`;

    const res = await fetch(`${API_BASE_URL}${path}`, { headers });
    if (!res.ok) {
        let mensagem = `Erro ${res.status} ao gerar relatório.`;
        try {
            const body = await res.json();
            if (body?.mensagem) mensagem = body.mensagem;
        } catch { /* sem corpo JSON */ }
        throw new Error(mensagem);
    }

    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    // Libera o blob depois de um tick para o browser iniciar o download
    setTimeout(() => URL.revokeObjectURL(url), 1000);
};

/** Relatorio geral de pedidos (todos os status). */
export const baixarRelatorioPedidos = () =>
    baixarPdf("/api/relatorios/pedidos", "relatorio-pedidos.pdf");

/** Relatorio de pedidos filtrado por status (PENDENTE/APROVADO/REJEITADO/PRONTO). */
export const baixarRelatorioPedidosPorStatus = (status) =>
    baixarPdf(`/api/relatorios/pedidos/status?status=${encodeURIComponent(status)}`,
              `relatorio-pedidos-${status.toLowerCase()}.pdf`);

/** Catalogo de produtos. */
export const baixarRelatorioProdutos = () =>
    baixarPdf("/api/relatorios/produtos", "relatorio-produtos.pdf");
