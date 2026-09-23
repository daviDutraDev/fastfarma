import "./DashBoard.css";
import { useEffect, useState, useMemo } from "react";
import { BuscarPedidos, BuscarPedidosPorStatus } from "../../services/api/Pedidos";
import { BuscarProdutos } from "../../services/api/Produtos";

function Dashboard() {
    const [pedidos, setPedidos] = useState([]);
    const [produtos, setProdutos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const carregar = async () => {
            setLoading(true);
            setError(null);
            try {
                const [pedidosResp, produtosResp] = await Promise.all([
                    BuscarPedidos(),
                    BuscarProdutos(),
                ]);
                setPedidos(Array.isArray(pedidosResp?.dados) ? pedidosResp.dados : []);
                setProdutos(Array.isArray(produtosResp?.dados) ? produtosResp.dados : []);
            } catch (e) {
                setError(e.message || "Erro ao carregar dashboard");
            } finally {
                setLoading(false);
            }
        };
        carregar();
    }, []);

    const stats = useMemo(() => {
        const total = pedidos.length;
        const pendentes = pedidos.filter(p => p.status === "PENDENTE").length;
        const aprovados = pedidos.filter(p => p.status === "APROVADO").length;
        const prontas = pedidos.filter(p => p.status === "PRONTO").length;

        const receita = pedidos
            .filter(p => p.status !== "REJEITADO")
            .reduce((acc, p) => {
                const valor = Number(p.valorTotal ?? 0);
                return acc + (Number.isFinite(valor) ? valor : 0);
            }, 0);

        const estoqueBaixo = produtos.filter(
            p => (p.estoque ?? 0) > 0 && (p.estoque ?? 0) <= 5
        ).length;

        return { total, pendentes, aprovados, prontas, receita, estoqueBaixo };
    }, [pedidos, produtos]);

    if (loading) {
        return <div className="dashboard"><p className="loading">Carregando...</p></div>;
    }

    return (
        <div className="dashboard">

            {error && <div className="mensagem erro">{error}</div>}

            <div className="dashboard-content">

                <section className="cards-container">

                    <div className="dashboard-card total">
                        <span>Total de pedidos</span>
                        <strong>{stats.total}</strong>
                    </div>

                    <div className="dashboard-card pendentes">
                        <span>Pendentes</span>
                        <strong>{stats.pendentes}</strong>
                    </div>

                    <div className="dashboard-card aprovados">
                        <span>Aprovados / Prontos</span>
                        <strong>{stats.aprovados + stats.prontas}</strong>
                    </div>

                    <div className="dashboard-card receita">
                        <span>Receita prevista</span>
                        <strong>
                            {stats.receita.toLocaleString("pt-BR", {
                                style: "currency",
                                currency: "BRL",
                            })}
                        </strong>
                    </div>

                    <div className="dashboard-card estoque">
                        <span>Estoque baixo</span>
                        <strong>{stats.estoqueBaixo}</strong>
                    </div>

                </section>

                <section className="pedidos-box">

                    <div className="pedidos-box-header">
                        <h2>Pedidos recentes</h2>
                        <span>{pedidos.length} total</span>
                    </div>

                    {pedidos.length === 0 ? (
                        <p className="sem-pedidos">Nenhum pedido cadastrado ainda.</p>
                    ) : (
                        <table className="pedidos-table">

                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Cliente</th>
                                    <th>Status</th>
                                    <th>Itens</th>
                                    <th>Valor</th>
                                    <th>Código</th>
                                </tr>
                            </thead>

                            <tbody>
                                {pedidos.slice(0, 10).map((pedido) => (
                                    <tr key={pedido.id}>

                                        <td>#{pedido.id}</td>
                                        <td>{pedido.criadoPor}</td>

                                        <td>
                                            <span className={`status status-${(pedido.status || "").toLowerCase()}`}>
                                                {pedido.status}
                                            </span>
                                        </td>

                                        <td>
                                            {(pedido.itens?.length ?? 0)} item(s)
                                        </td>

                                        <td>
                                            {(Number(pedido.valorTotal ?? 0)).toLocaleString("pt-BR", {
                                                style: "currency",
                                                currency: "BRL",
                                            })}
                                        </td>

                                        <td>{pedido.codigoVerificacao}</td>

                                    </tr>
                                ))}
                            </tbody>

                        </table>
                    )}

                </section>

            </div>
        </div>
    );
}

export default Dashboard;
