import { useEffect, useState } from "react";
import { BuscarProdutos } from "../../../services/api/Produtos";
import ModalTelefone from "../../../components/telefone/ModalTelefone";
import "./FazerPedido.css";

function FazerPedido() {
  const [produtos, setProdutos] = useState([]);
  const [carrinho, setCarrinho] = useState([]);

  const [modalTelefoneAberto, setModalTelefoneAberto] =
  useState(false);

  const [produtoSelecionado, setProdutoSelecionado] =
    useState(null);

  const [itemSelecionado, setItemSelecionado] =
    useState(null);

  const [busca, setBusca] = useState("");
  const [categoria, setCategoria] = useState("todas");

  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    carregarProdutos();
  }, []);

  const carregarProdutos = async () => {
    try {
      setLoading(true);
      setErro(null);

      const data = await BuscarProdutos();

      setProdutos(data.dados || []);
    } catch (error) {
      setErro(error.message || "Erro ao carregar produtos");
    } finally {
      setLoading(false);
    }
  };

  const produtosFiltrados = produtos.filter((produto) => {
    const nomeEncontrado = produto.nome
      .toLowerCase()
      .includes(busca.toLowerCase());

    const categoriaEncontrada =
      categoria === "todas" ||
      produto.categoria === categoria;

    return nomeEncontrado && categoriaEncontrada;
  });

  const categorias = [
    ...new Set(produtos.map((produto) => produto.categoria)),
  ];

  const adicionarAoCarrinho = () => {
    if (!produtoSelecionado) {
      alert("Selecione um produto.");
      return;
    }

    if (produtoSelecionado.estoque <= 0) {
      alert("Esse produto está sem estoque.");
      return;
    }

    const produtoJaAdicionado = carrinho.find(
      (item) => item.id === produtoSelecionado.id
    );

    if (produtoJaAdicionado) {
      if (
        produtoJaAdicionado.quantidade >=
        produtoSelecionado.estoque
      ) {
        alert("Quantidade máxima disponível atingida.");
        return;
      }

      setCarrinho(
        carrinho.map((item) =>
          item.id === produtoSelecionado.id
            ? {
                ...item,
                quantidade: item.quantidade + 1,
              }
            : item
        )
      );
    } else {
      setCarrinho([
        ...carrinho,
        {
          ...produtoSelecionado,
          quantidade: 1,
        },
      ]);
    }

    setProdutoSelecionado(null);
  };

  const alterarQuantidade = (produtoId, novaQuantidade) => {
    const quantidade = Number(novaQuantidade);

    setCarrinho(
      carrinho.map((item) => {
        if (item.id !== produtoId) {
          return item;
        }

        if (quantidade < 1) {
          return item;
        }

        if (quantidade > item.estoque) {
          alert("Quantidade maior que o estoque disponível.");
          return item;
        }

        return {
          ...item,
          quantidade,
        };
      })
    );
  };

  const removerItem = () => {
    if (!itemSelecionado) {
      alert("Selecione um item do carrinho.");
      return;
    }

    setCarrinho(
      carrinho.filter(
        (item) => item.id !== itemSelecionado.id
      )
    );

    setItemSelecionado(null);
  };

  const total = carrinho.reduce((soma, item) => {
  return soma + Number(item.preco) * item.quantidade;
}, 0);

const finalizarPedido = () => {
  if (carrinho.length === 0) {
    alert("O carrinho está vazio.");
    return;
  }

  setModalTelefoneAberto(true);
};


//pedido whats
const enviarPedidoWhatsApp = (telefone) => {
  const telefoneComPais = `55${telefone}`;

  const produtosMensagem = carrinho
    .map((item) => {
      const subtotal =
        Number(item.preco) * item.quantidade;

      return (
        `• ${item.nome}\n` +
        `Quantidade: ${item.quantidade}\n` +
        `Subtotal: ${subtotal.toLocaleString("pt-BR", {
          style: "currency",
          currency: "BRL",
        })}`
      );
    })
    .join("\n\n");

  const mensagem =
    `*Pedido FastFarma*\n\n` +
    `${produtosMensagem}\n\n` +
    `*Total: ${total.toLocaleString("pt-BR", {
      style: "currency",
      currency: "BRL",
    })}*`;

  const mensagemCodificada = encodeURIComponent(mensagem);

  const urlWhatsApp =
    `https://wa.me/${telefoneComPais}` +
    `?text=${mensagemCodificada}`;

  window.open(urlWhatsApp, "_blank");

  setModalTelefoneAberto(false);
};

if (loading) {
  return (
    <p className="pedido-mensagem">
      Carregando produtos...
    </p>
  );
}

if (erro) {
  return <p className="pedido-erro">{erro}</p>;
}

  return (
    <section className="fazer-pedido">
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
                <tr
                  key={produto.id}
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
                  }
                >
                  <td>{produto.id}</td>
                  <td>{produto.nome}</td>
                  <td>{produto.categoria}</td>

                  <td>
                    {Number(produto.preco).toLocaleString(
                      "pt-BR",
                      {
                        style: "currency",
                        currency: "BRL",
                      }
                    )}
                  </td>

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
          <input
            type="text"
            placeholder="Buscar produto por nome..."
            value={busca}
            onChange={(event) => setBusca(event.target.value)}
          />

          <select
            value={categoria}
            onChange={(event) =>
              setCategoria(event.target.value)
            }
          >
            <option value="todas">Todas as categorias</option>

            {categorias.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>

          <button
            className="botao-adicionar"
            onClick={adicionarAoCarrinho}
            disabled={!produtoSelecionado}
          >
            + Adicionar ao carrinho
          </button>
        </div>
      </div>

      <div className="pedido-coluna carrinho">
        <div className="pedido-titulo">
          <h2>Carrinho</h2>
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
                <tr
                  key={item.id}
                  className={
                    itemSelecionado?.id === item.id
                      ? "linha-selecionada"
                      : ""
                  }
                  onClick={() =>
                    setItemSelecionado(
                      itemSelecionado?.id === item.id
                        ? null
                        : item
                    )
                  }
                >
                  <td>{item.nome}</td>

                  <td>
                    {Number(item.preco).toLocaleString(
                      "pt-BR",
                      {
                        style: "currency",
                        currency: "BRL",
                      }
                    )}
                  </td>

                  <td>
                    <input
                      className="quantidade"
                      type="number"
                      min="1"
                      max={item.estoque}
                      value={item.quantidade}
                      onClick={(event) =>
                        event.stopPropagation()
                      }
                      onChange={(event) =>
                        alterarQuantidade(
                          item.id,
                          event.target.value
                        )
                      }
                    />
                  </td>

                  <td>
                    {(
                      Number(item.preco) * item.quantidade
                    ).toLocaleString("pt-BR", {
                      style: "currency",
                      currency: "BRL",
                    })}
                  </td>
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
          <strong>
            Total:{" "}
            {total.toLocaleString("pt-BR", {
              style: "currency",
              currency: "BRL",
            })}
          </strong>

          <button
            className="botao-remover"
            onClick={removerItem}
            disabled={!itemSelecionado}
          >
            Remover item
          </button>

          <button
            className="botao-finalizar"
            onClick={finalizarPedido}
            disabled={carrinho.length === 0}
          >
            Finalizar pedido
          </button>
        </div>
      </div>

      <ModalTelefone
      aberto={modalTelefoneAberto}
      onClose={() => setModalTelefoneAberto(false)}
      onConfirmar={enviarPedidoWhatsApp}
      total={total}
    />
    </section>
  );
}
export default FazerPedido;