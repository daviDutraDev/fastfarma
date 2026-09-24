import { useEffect, useState } from "react";
import { BuscarProdutos } from "../../../services/api/Produtos";
import { CriarPedido } from "../../../services/api/Pedidos";
import { useAuth } from "../../../auth/AuthContext.jsx";
import "./FazerPedido.css";

function FazerPedido() {
    const { user, isAuthenticated } = useAuth();

    const [produtos, setProdutos] = useState([]);
    const [carrinho, setCarrinho] = useState([]);

    const [busca, setBusca] = useState("");
    const [categoria, setCategoria] = useState("todas");

    const [produtoSelecionado, setProdutoSelecionado] = useState(null);
    const [itemSelecionado, setItemSelecionado] = useState(null);

    const [loading, setLoading] = useState(true);
    const [loadingFinalizar, setLoadingFinalizar] = useState(false);
    const [error, setError] = useState(null);
    const [mensagem, setMensagem] = useState(null);

    // Resultado do pedido criado (mostra codigo + status)
    const [pedidoCriado, setPedidoCriado] = useState(null);

    useEffect(() => {
        carregarProdutos();
    }, []);

    const carregarProdutos = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await BuscarProdutos();
            setProdutos(Array.isArray(data?.dados) ? data.dados : []);
        } catch (e) {
            setError(e.message || "Erro ao carregar produtos");
        } finally {
            setLoading(false);
        }
    };

    const produtosFiltrados = produtos.filter((produto) => {
        const nomeOk = produto.nome.toLowerCase().includes(busca.toLowerCase());
        const categoriaOk = categoria === "todas" || produto.categoria === categoria;
        return nomeOk && categoriaOk;
    });

    const categorias = [...new Set(produtos.map((p) => p.categoria).filter(Boolean))];

    const adicionarAoCarrinho = () => {
        if (!produtoSelecionado) {
            setError("Selecione um produto primeiro.");
            return;
        }
        if (produtoSelecionado.estoque <= 0) {
            setError("Esse produto está sem estoque.");
            return;
        }

        const jaExiste = carrinho.find((i) => i.id === produtoSelecionado.id);
        if (jaExiste) {
            if (jaExiste.quantidade >= produtoSelecionado.estoque) {
                setError("Quantidade máxima disponível atingida.");
                return;
            }
            setCarrinho(carrinho.map((i) =>
                i.id === produtoSelecionado.id
                    ? { ...i, quantidade: i.quantidade + 1 }
                    : i));
        } else {
            setCarrinho([...carrinho, { ...produtoSelecionado, quantidade: 1 }]);
        }
        setProdutoSelecionado(null);
        setError(null);
    };

    const alterarQuantidade = (produtoId, novaQtd) => {
        const q = Number(novaQtd);
        setCarrinho(carrinho.map((item) => {
            if (item.id !== produtoId) return item;
            if (q < 1) return item;
            if (q > item.estoque) {
                setError("Quantidade maior que o estoque disponível.");
                return item;
            }
            return { ...item, quantidade: q };
        }));
    };

    const removerItem = () => {
        if (!itemSelecionado) {
            setError("Selecione um item do carrinho primeiro.");
            return;
        }
        setCarrinho(carrinho.filter((i) => i.id !== itemSelecionado.id));
        setItemSelecionado(null);
    };

    const total = carrinho.reduce((s, i) => s + Number(i.preco) * i.quantidade, 0);

    const finalizarPedido = async () => {
        if (carrinho.length === 0) {
            setError("O carrinho está vazio.");
            return;
        }
        if (!isAuthenticated || !user?.nome) {
            setError("Você precisa estar logado para finalizar o pedido.");
            return;
        }

        setLoadingFinalizar(true);
        setError(null);
        setMensagem(null);
        try {
            // Deduplica: o schema atual do pedido nao tem coluna de
            // quantidade por item, entao cada produto = 1 unidade no
            // pedido. O carrinho permite quantidade para o cliente
            // controlar o limite de estoque, mas so mandamos o id
            // uma vez para o backend nao recusar com
            // "Produto X ja esta no pedido".
            const idsUnicos = [...new Set(carrinho.map((i) => i.id))];

            // O telefone para o WhatsApp e o que ja esta salvo no perfil
            // (editavel em "Perfil"). O backend le direto do usuario.

            const resp = await CriarPedido(idsUnicos);
            const pedido = resp?.dados;
            setPedidoCriado(pedido);
            setMensagem(`Pedido #${pedido?.id} criado com sucesso!`);
            setCarrinho([]);
        } catch (e) {
            setError(e.message || "Erro ao criar pedido.");
        } finally {
            setLoadingFinalizar(false);
        }
    };

    const formatarMoeda = (v) =>
        Number(v).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

    const novoPedido = () => {
        setPedidoCriado(null);
        setMensagem(null);
    };

    if (loading) {
        return <p className="pedido-mensagem">Carregando produtos...</p>;
    }
    if (error && !produtos.length) {
        return <p className="pedido-erro">{error}</p>;
    }

    // Tela de sucesso apos criar o pedido
    if (pedidoCriado) {
        return (
            <section className="fazer-pedido">
                <div className="pedido-sucesso-card">
                    <div className="sucesso-icone">✓</div>
                    <h2>Pedido criado!</h2>
                    <p className="sucesso-subtitulo">
                        Seu pedido foi registrado. Acompanhe pelo menu "Meus Pedidos".
                    </p>

                    <div className="sucesso-info">
                        <div>
                            <span>Número</span>
                            <strong>#{pedidoCriado.id}</strong>
                        </div>
                        <div>
                            <span>Código de retirada</span>
                            <strong className="codigo-destaque">
                                {pedidoCriado.codigoVerificacao}
                            </strong>
                        </div>
                        <div>
                            <span>Status</span>
                            <strong>{pedidoCriado.status}</strong>
                        </div>
                        <div>
                            <span>Total</span>
                            <strong>{formatarMoeda(pedidoCriado.valorTotal)}</strong>
                        </div>
                    </div>

                    <div className="sucesso-info-aviso">
                        📱 Você receberá uma notificação no WhatsApp
                        quando o pedido estiver pronto para retirada.
                    </div>

                    <div className="sucesso-botoes">
                        <button className="botao-finalizar" onClick={novoPedido}>
                            Fazer outro pedido
                        </button>
                    </div>
                </div>
            </section>
        );
    }

    return (
        <section className="fazer-pedido">

            {mensagem && <div className="pedido-feedback sucesso">{mensagem}</div>}
            {error && <div className="pedido-feedback erro">{error}</div>}

            <div className="pedido-coluna produtos-disponiveis">
                <div className="pedido-titulo">
                    <h2>Produtos disponíveis</h2>
                    <span>Selecione e clique em Adicionar</span>
                </div>

                <div className="tabela-container">
                    <table className="pedido-tabela">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome</th>
                                <th>Categoria</th>
                                <th>Preço</th>
                                <th>Estoque</th>
                            </tr>
                        </thead>

                        <tbody>
                            {produtosFiltrados.map((produto) => (
                                <tr key={produto.id}
                                    className={
                                        produtoSelecionado?.id === produto.id
                                            ? "linha-selecionada"
                                            : ""
                                    }
                                    onClick={() =>
                                        setProdutoSelecionado(
                                            produtoSelecionado?.id === produto.id
                                                ? null
                                                : produto
                                        )
                                    }>
                                    <td>{produto.id}</td>
                                    <td>{produto.nome}</td>
                                    <td>{produto.categoria || "-"}</td>
                                    <td>{formatarMoeda(produto.preco)}</td>
                                    <td>{produto.estoque}</td>
                                </tr>
                            ))}

                            {produtosFiltrados.length === 0 && (
                                <tr>
                                    <td colSpan="5" className="tabela-vazia">
                                        Nenhum produto encontrado.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="produtos-rodape">
                    <input type="text" placeholder="Buscar produto por nome..."
                           value={busca}
                           onChange={(e) => setBusca(e.target.value)} />

                    <select value={categoria}
                            onChange={(e) => setCategoria(e.target.value)}>
                        <option value="todas">Todas as categorias</option>
                        {categorias.map((c) => (
                            <option key={c} value={c}>{c}</option>
                        ))}
                    </select>

                    <button className="botao-adicionar"
                            onClick={adicionarAoCarrinho}
                            disabled={!produtoSelecionado}>
                        + Adicionar ao carrinho
                    </button>
                </div>
            </div>

            <div className="pedido-coluna carrinho">
                <div className="pedido-titulo">
                    <h2>Carrinho</h2>
                    <span>
                        {carrinho.length} {carrinho.length === 1 ? "item" : "itens"}
                    </span>
                </div>

                <div className="tabela-container">
                    <table className="pedido-tabela">
                        <thead>
                            <tr>
                                <th>Produto</th>
                                <th>Preço</th>
                                <th>Quantidade</th>
                                <th>Subtotal</th>
                            </tr>
                        </thead>

                        <tbody>
                            {carrinho.map((item) => (
                                <tr key={item.id}
                                    className={
                                        itemSelecionado?.id === item.id
                                            ? "linha-selecionada" : ""
                                    }
                                    onClick={() =>
                                        setItemSelecionado(
                                            itemSelecionado?.id === item.id
                                                ? null : item
                                        )
                                    }>
                                    <td>{item.nome}</td>
                                    <td>{formatarMoeda(item.preco)}</td>
                                    <td>
                                        <input className="quantidade"
                                               type="number"
                                               min="1"
                                               max={item.estoque}
                                               value={item.quantidade}
                                               onClick={(e) => e.stopPropagation()}
                                               onChange={(e) =>
                                                   alterarQuantidade(item.id, e.target.value)
                                               } />
                                    </td>
                                    <td>{formatarMoeda(Number(item.preco) * item.quantidade)}</td>
                                </tr>
                            ))}

                            {carrinho.length === 0 && (
                                <tr>
                                    <td colSpan="4" className="tabela-vazia">
                                        Nenhum produto adicionado.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="carrinho-rodape">
                    <strong>Total: {formatarMoeda(total)}</strong>

                    <button className="botao-remover"
                            onClick={removerItem}
                            disabled={!itemSelecionado}>
                        Remover item
                    </button>

                    <button className="botao-finalizar"
                            onClick={finalizarPedido}
                            disabled={carrinho.length === 0 || loadingFinalizar}>
                        {loadingFinalizar ? "Finalizando..." : "Finalizar pedido"}
                    </button>
                </div>

                <p className="carrinho-aviso">
                    Cada item do carrinho conta como 1 unidade do produto no pedido.
                    A quantidade maxima respeita o estoque disponivel.
                </p>
            </div>

        </section>
    );
}

export default FazerPedido;