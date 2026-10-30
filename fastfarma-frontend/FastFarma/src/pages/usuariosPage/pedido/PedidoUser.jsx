import "./PedidoUser.css";
import { useEffect, useState } from "react";
import { BuscarPedidosUsuario } from "../../../services/api/Pedidos";
import { useAuth } from "../../../auth/AuthContext.jsx";
import ModalPedido from "../../../components/pedidos/ModalPedido.jsx";

function PedidoUser() {
    const { user } = useAuth();
    const [pedidos, setPedidos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [pedidoSelecionado, setPedidoSelecionado] = useState(null);

    useEffect(() => {
        const carregar = async () => {
            if (!user?.nome) {
                setError("Usuário não identificado. Faça login novamente.");
                setLoading(false);
                return;
            }
            try {
                setLoading(true);
                setPedidoSelecionado(null);
                setError(null);
                const data = await BuscarPedidosUsuario(user.nome);
                setPedidos(Array.isArray(data?.dados) ? data.dados : []);
            } catch (e) {
                setError(e.message || "Não foi possível carregar seus pedidos.");
                setPedidos([]);
            } finally {
                setLoading(false);
            }
        };
        carregar();
    }, [user?.nome]);

    const formatarMoeda = (v) =>
        Number(v ?? 0).toLocaleString("pt-BR", {
            style: "currency",
            currency: "BRL",
        });

    const formatarQuantidade = (itens) => {
        const qtd = Array.isArray(itens)
            ? itens.reduce((soma, i) => soma + (i.quantidade ?? 1), 0)
            : 0;
        return `${qtd} ${qtd === 1 ? "item" : "itens"}`;
    };

    if (loading) {
        return (
            <section className="meus-pedidos">
                <div className="estado-pedidos carregando-pedidos">
                    <div className="loading-spinner"></div>
                    <p>Carregando seus pedidos...</p>
                </div>
            </section>
        );
    }

    if (error) {
        return (
            <section className="meus-pedidos">
                <div className="estado-pedidos erro-pedidos">
                    <h3>Não foi possível carregar os pedidos</h3>
                    <p>{error}</p>
                </div>
            </section>
        );
    }

    return (
        <section className="meus-pedidos">
            <div className="pedidos-card">
                <div className="pedidos-card-header">
                    <div>
                        <h2>Meus pedidos</h2>
                        <p>Acompanhe o andamento dos seus pedidos.</p>
                        <p id="meus-pedidos-instrucao">Dê um clique duplo na linha para ver os detalhes, ou selecione-a e pressione Enter.</p>
                    </div>

                    <span className="quantidade-pedidos">
                        {pedidos.length}{" "}
                        {pedidos.length === 1 ? "pedido" : "pedidos"}
                    </span>
                </div>

                {pedidos.length > 0 ? (
                    <div className="tabela-container">
                        <table className="tabela-pedidos">
                            <thead>
                                <tr>
                                    <th>#ID</th>
                                    <th>Status</th>
                                    <th>Itens</th>
                                    <th>Valor</th>
                                    <th>Código de retirada</th>
                                </tr>
                            </thead>

                            <tbody>
                                {pedidos.map((pedido) => (
                                    <tr key={pedido.id} className="meu-pedido-detalhavel" tabIndex={0}
                                        aria-haspopup="dialog" aria-describedby="meus-pedidos-instrucao"
                                        onDoubleClick={() => setPedidoSelecionado(pedido)}
                                        onKeyDown={(event) => {
                                            if (event.key === "Enter" || event.key === " ") {
                                                event.preventDefault();
                                                setPedidoSelecionado(pedido);
                                            }
                                        }}>
                                        <td className="pedido-id">#{pedido.id}</td>

                                        <td>
                                            <span
                                                className={`status-pedido ${(
                                                    pedido.status || "pendente"
                                                ).toLowerCase()}`}
                                            >
                                                {pedido.status || "PENDENTE"}
                                            </span>
                                        </td>

                                        <td>{formatarQuantidade(pedido.itens)}</td>

                                        <td className="pedido-valor">
                                            {formatarMoeda(pedido.valorTotal)}
                                        </td>

                                        <td>
                                            <strong className="codigo-retirada">
                                                {pedido.codigoVerificacao ?? "Aguardando"}
                                            </strong>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <div className="pedidos-vazio">
                        <div className="icone-pedido-vazio">!</div>
                        <h3>Nenhum pedido encontrado</h3>
                        <p>
                            Quando você fizer um pedido, ele aparecerá aqui.
                        </p>
                    </div>
                )}
            </div>
            {pedidoSelecionado && (
                <ModalPedido pedido={pedidoSelecionado} onClose={() => setPedidoSelecionado(null)} />
            )}
        </section>
    );
}

export default PedidoUser;
