import { useState } from "react";
import { FaClipboardList, FaFilter, FaBoxes, FaChartLine, FaChartPie, FaTruckLoading, FaFilePdf, FaCalendarAlt } from "react-icons/fa";
import {
    baixarRelatorioPedidos, baixarRelatorioPedidosPorStatus, baixarRelatorioProdutos,
    baixarRelatorioDesempenho, baixarRelatorioGeral, baixarRelatorioReposicao,
} from "../../services/api/Relatorios";
import { obterPeriodo, formatarData } from "./periodoRelatorio.js";
import "./Relatorio.css";

const STATUS_OPCOES = ["PENDENTE", "APROVADO", "REJEITADO", "PRONTO"];

function Relatorio() {
    const [statusFiltro, setStatusFiltro] = useState("PENDENTE");
    const [periodo, setPeriodo] = useState("30dias");
    const [datas, setDatas] = useState(() => obterPeriodo("30dias"));
    const [cobertura, setCobertura] = useState(30);
    const [loading, setLoading] = useState(null);
    const [mensagem, setMensagem] = useState(null);
    const [error, setError] = useState(null);
    const hoje = formatarData(new Date());
    const efetivas = periodo === "personalizado" ? datas : obterPeriodo(periodo);
    const erroPeriodo = periodo !== "todo" && (!efetivas.inicio || !efetivas.fim
        ? "Preencha a data inicial e a final."
        : efetivas.inicio > efetivas.fim ? "A data inicial deve ser anterior ou igual à final."
        : efetivas.inicio < "1900-01-01" || efetivas.fim > hoje ? "Escolha datas entre 1900 e hoje." : "");
    const coberturaInvalida = !Number.isInteger(Number(cobertura)) || Number(cobertura) < 1 || Number(cobertura) > 365;
    const inicio = efetivas.inicio || undefined;
    const fim = efetivas.fim || undefined;

    const trocarPeriodo = (valor) => {
        setPeriodo(valor);
        if (valor !== "personalizado") setDatas(obterPeriodo(valor));
        else if (!datas.inicio || !datas.fim) setDatas(obterPeriodo("30dias"));
        setMensagem(null); setError(null);
    };

    const gerar = async (rotulo, acao, usaPeriodo = true, reposicao = false) => {
        if (loading) return;
        if (usaPeriodo && erroPeriodo) { setError(erroPeriodo); return; }
        if (reposicao && coberturaInvalida) { setError("Informe uma cobertura de 1 a 365 dias."); return; }
        setLoading(rotulo); setMensagem(null); setError(null);
        try {
            await acao();
            setMensagem(`Download de “${rotulo}” iniciado.`);
        } catch (e) { setError(e.message || `Erro ao gerar “${rotulo}”.`); }
        finally { setLoading(null); }
    };

    const cartoes = [
        { id: "pedidos", numero: "01", nome: "Pedidos (todos)", icone: FaClipboardList,
            descricao: "Listagem dos pedidos com cliente, status, quantidade, valor e data.",
            detalhes: "Todos os status · Período selecionado", acao: () => baixarRelatorioPedidos(inicio, fim) },
        { id: "status", numero: "02", nome: "Pedidos por status", icone: FaFilter,
            descricao: "Consulte uma etapa específica e acompanhe os pedidos do período.",
            detalhes: "Status escolhido · Período selecionado", acao: () => baixarRelatorioPedidosPorStatus(statusFiltro, inicio, fim) },
        { id: "catalogo", numero: "03", nome: "Catálogo de produtos", icone: FaBoxes,
            descricao: "Produtos cadastrados, categorias, preços e disponibilidade no estoque atual.",
            detalhes: "Posição atual · Independente do filtro de datas", acao: baixarRelatorioProdutos },
        { id: "desempenho", numero: "04", nome: "Desempenho de pedidos", icone: FaChartLine,
            descricao: "Valor médio por pedido, evolução e rankings completos de produtos e clientes.",
            detalhes: "Gráficos · Valores e rankings sem rejeitados", acao: () => baixarRelatorioDesempenho(inicio, fim) },
        { id: "geral", numero: "05", nome: "Geral da farmácia", icone: FaChartPie,
            descricao: "Visão consolidada com indicadores, comparação de períodos, destaques e alertas de estoque.",
            detalhes: "Resumo executivo · Estoque na data de geração", acao: () => baixarRelatorioGeral(inicio, fim) },
        { id: "reposicao", numero: "06", nome: "Reposição de estoque", icone: FaTruckLoading,
            descricao: "Demanda média, cobertura estimada e sugestão de quantidade para repor por produto.",
            detalhes: "Projeção de demanda · Estoque atual", acao: () => baixarRelatorioReposicao(inicio, fim, Number(cobertura)) },
    ];

    return (
        <div className="relatorio-page" aria-busy={loading !== null}>
            <header className="relatorio-header">
                <h1>Relatórios</h1>
                <p>Consulte a operação e transforme os pedidos em informações para planejar.</p>
            </header>
            <section className="relatorio-periodo" aria-labelledby="relatorio-periodo-titulo">
                <div className="relatorio-periodo-titulo">
                    <FaCalendarAlt aria-hidden="true" />
                    <div><h2 id="relatorio-periodo-titulo">Período de análise</h2><p>Aplicado aos relatórios de pedidos, desempenho, geral e reposição.</p></div>
                </div>
                <div className="relatorio-periodo-campos">
                    <label>Período
                        <select value={periodo} onChange={(e) => trocarPeriodo(e.target.value)} disabled={loading !== null}>
                            <option value="hoje">Hoje</option><option value="7dias">Últimos 7 dias</option>
                            <option value="30dias">Últimos 30 dias</option><option value="mes">Este mês</option>
                            <option value="todo">Todo o período</option><option value="personalizado">Personalizado</option>
                        </select>
                    </label>
                    {periodo !== "todo" && <>
                        <label>Data inicial<input type="date" min="1900-01-01" max={hoje} value={efetivas.inicio}
                            disabled={loading !== null || periodo !== "personalizado"}
                            onChange={(e) => setDatas({ ...datas, inicio: e.target.value })} /></label>
                        <label>Data final<input type="date" min={efetivas.inicio || "1900-01-01"} max={hoje} value={efetivas.fim}
                            disabled={loading !== null || periodo !== "personalizado"}
                            onChange={(e) => setDatas({ ...datas, fim: e.target.value })} /></label>
                    </>}
                </div>
                {erroPeriodo && <p className="relatorio-validacao" role="alert">{erroPeriodo}</p>}
                {periodo === "todo" && <p className="relatorio-ajuda">Todo o histórico registrado. A comparação com período anterior não se aplica.</p>}
            </section>
            {mensagem && <div role="status" className="relatorio-msg sucesso">{mensagem}</div>}
            {error && <div role="alert" className="relatorio-msg erro">{error}</div>}
            <section className="relatorio-grid" aria-label="Relatórios disponíveis">
                {cartoes.map(({ id, numero, nome, icone: Icone, descricao, detalhes, acao }) => (
                    <article key={id} className={`relatorio-card ${id === "geral" ? "relatorio-card-destaque" : ""}`}>
                        <div className="relatorio-card-topo"><div className="relatorio-card-icone"><Icone aria-hidden="true" /></div>
                            <span className="relatorio-card-numero">{id === "geral" ? "PRINCIPAL" : numero}</span></div>
                        <h2>{nome}</h2><p>{descricao}</p>
                        {id === "status" && <div className="relatorio-card-filtros">
                            <label htmlFor="status">Status:</label>
                            <select id="status" value={statusFiltro} disabled={loading !== null} onChange={(e) => setStatusFiltro(e.target.value)}>
                                {STATUS_OPCOES.map((s) => <option key={s} value={s}>{s}</option>)}
                            </select>
                        </div>}
                        {id === "reposicao" && <div className="relatorio-cobertura">
                            <label htmlFor="relatorio-cobertura">Cobertura desejada (dias)</label>
                            <input id="relatorio-cobertura" type="number" min="1" max="365" step="1" value={cobertura}
                                aria-invalid={coberturaInvalida} aria-describedby="relatorio-cobertura-ajuda"
                                disabled={loading !== null} onChange={(e) => setCobertura(e.target.value)} />
                            <span id="relatorio-cobertura-ajuda" className={coberturaInvalida ? "relatorio-validacao" : "relatorio-ajuda"}>
                                {coberturaInvalida ? "Informe de 1 a 365 dias." : "A estimativa usa o período escolhido acima. Padrão: últimos 30 dias."}
                            </span>
                        </div>}
                        <span className="relatorio-card-detalhes">{detalhes}</span>
                        <button type="button" className="btn-relatorio"
                            disabled={loading !== null || (id !== "catalogo" && Boolean(erroPeriodo)) || (id === "reposicao" && coberturaInvalida)}
                            onClick={() => gerar(nome, acao, id !== "catalogo", id === "reposicao")}>
                            <FaFilePdf aria-hidden="true" /> {loading === nome ? "Gerando PDF..." : "Baixar PDF"}
                        </button>
                    </article>
                ))}
            </section>
            <p className="relatorio-nota">Valores baseados nos preços atuais do catálogo, sem confirmação de pagamento.
                O estoque representa a posição na data de geração. Sugestões de reposição são estimativas para revisão.</p>
        </div>
    );
}

export default Relatorio;
