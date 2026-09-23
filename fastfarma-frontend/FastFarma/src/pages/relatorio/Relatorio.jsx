import { useState } from "react";
import {
    baixarRelatorioPedidos,
    baixarRelatorioPedidosPorStatus,
    baixarRelatorioProdutos,
} from "../../services/api/Relatorios";
import "./Relatorio.css";

const STATUS_OPCOES = ["PENDENTE", "APROVADO", "REJEITADO", "PRONTO"];

function Relatorio() {
    const [statusFiltro, setStatusFiltro] = useState("PENDENTE");
    const [loading, setLoading] = useState(null);
    const [mensagem, setMensagem] = useState(null);
    const [error, setError] = useState(null);

    const gerar = async (rotulo, acao) => {
        setLoading(rotulo);
        setMensagem(null);
        setError(null);
        try {
            await acao();
            setMensagem(`"${rotulo}" gerado com sucesso.`);
        } catch (e) {
            setError(e.message || `Erro ao gerar "${rotulo}".`);
        } finally {
            setLoading(null);
        }
    };

    return (
        <div className="relatorio-page">

            <header className="relatorio-header">
                <h1>Relatórios</h1>
                <p>Baixe relatórios em PDF com os dados mais recentes do sistema.</p>
            </header>

            {mensagem && <div className="relatorio-msg sucesso">{mensagem}</div>}
            {error && <div className="relatorio-msg erro">{error}</div>}

            <section className="relatorio-grid">

                {/* Card 1: Pedidos (todos) */}
                <article className="relatorio-card">
                    <div className="relatorio-card-icone">📋</div>
                    <h2>Pedidos (todos)</h2>
                    <p>Lista completa de pedidos com cliente, status, itens e valor.</p>
                    <button
                        type="button"
                        className="btn-relatorio"
                        disabled={loading !== null}
                        onClick={() => gerar("Pedidos (todos)", baixarRelatorioPedidos)}
                    >
                        {loading === "Pedidos (todos)" ? "Gerando..." : "Baixar PDF"}
                    </button>
                </article>

                {/* Card 2: Pedidos por status */}
                <article className="relatorio-card">
                    <div className="relatorio-card-icone">🔍</div>
                    <h2>Pedidos por status</h2>
                    <p>Filtra os pedidos por status antes de gerar o PDF.</p>
                    <div className="relatorio-card-filtros">
                        <label htmlFor="status">Status:</label>
                        <select
                            id="status"
                            value={statusFiltro}
                            onChange={(e) => setStatusFiltro(e.target.value)}
                        >
                            {STATUS_OPCOES.map((s) => (
                                <option key={s} value={s}>{s}</option>
                            ))}
                        </select>
                    </div>
                    <button
                        type="button"
                        className="btn-relatorio"
                        disabled={loading !== null}
                        onClick={() =>
                            gerar(`Pedidos (${statusFiltro})`,
                                  () => baixarRelatorioPedidosPorStatus(statusFiltro))
                        }
                    >
                        {loading === `Pedidos (${statusFiltro})` ? "Gerando..." : "Baixar PDF"}
                    </button>
                </article>

                {/* Card 3: Produtos */}
                <article className="relatorio-card">
                    <div className="relatorio-card-icone">📦</div>
                    <h2>Catálogo de produtos</h2>
                    <p>Lista do catálogo com nome, categoria, preço e estoque.</p>
                    <button
                        type="button"
                        className="btn-relatorio"
                        disabled={loading !== null}
                        onClick={() => gerar("Catálogo de produtos", baixarRelatorioProdutos)}
                    >
                        {loading === "Catálogo de produtos" ? "Gerando..." : "Baixar PDF"}
                    </button>
                </article>

            </section>

        </div>
    );
}

export default Relatorio;
