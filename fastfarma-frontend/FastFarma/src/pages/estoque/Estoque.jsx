import { useEffect, useState } from "react";
import { FaPlus, FaTrash, FaFilePdf } from "react-icons/fa";

import { BuscarProdutos } from "../../services/api/Produtos";

import ModalNovoProduto from "../../components/estoque/ModalNovoProduto";
import ModalEditarProduto from "../../components/estoque/ModalEditarProduto";
import ModalExcluirProduto from "../../components/estoque/ModalExcluirProduto";

import "./Estoque.css";

function Estoque() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  const [busca, setBusca] = useState("");
  const [filtroEstoque, setFiltroEstoque] = useState("todos");

  // Produto alvo de cada modal - cada acao (editar / excluir) guarda o
  // seu proprio produto, entao clicar em excluir numa linha nao abre a
  // edicao de outra por engano.
  const [produtoEditando, setProdutoEditando] = useState(null);
  const [produtoExcluindo, setProdutoExcluindo] = useState(null);

  const [modalNovoAberto, setModalNovoAberto] = useState(false);
  const [modalEditarAberto, setModalEditarAberto] = useState(false);
  const [modalExcluirAberto, setModalExcluirAberto] = useState(false);

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
      console.error("Erro ao buscar produtos:", error);

      setErro(
        error.message || "Não foi possível carregar os produtos."
      );
    } finally {
      setLoading(false);
    }
  };

  const produtosFiltrados = produtos.filter((produto) => {
    const correspondeBusca = produto.nome
      .toLowerCase()
      .includes(busca.toLowerCase());

    let correspondeEstoque = true;

    if (filtroEstoque === "normal") {
      correspondeEstoque = produto.estoque > 5;
    }

    if (filtroEstoque === "baixo") {
      correspondeEstoque = produto.estoque > 0 && produto.estoque <= 5;
    }

    if (filtroEstoque === "sem-estoque") {
      correspondeEstoque = produto.estoque === 0;
    }

    return correspondeBusca && correspondeEstoque;
  });

  const obterSituacao = (quantidade) => {
    if (quantidade === 0) {
      return { texto: "Sem estoque", classe: "situacao-sem-estoque" };
    }

    if (quantidade <= 5) {
      return { texto: "Estoque baixo", classe: "situacao-baixo" };
    }

    return { texto: "Normal", classe: "situacao-normal" };
  };

  const abrirEdicao = (produto) => {
    setProdutoEditando(produto);
    setModalEditarAberto(true);
  };

  const abrirExclusao = (event, produto) => {
    // Impede que o clique "suba" para a linha e abra a edicao junto.
    event.stopPropagation();
    setProdutoExcluindo(produto);
    setModalExcluirAberto(true);
  };

  return (
    <section className="pagina-estoque">
      <div className="estoque-header">
        <div>
          <h1>Gestão de Estoque</h1>
          <p>Gerencie os produtos e suas quantidades.</p>
        </div>

        <button className="btn-relatorio">
          <FaFilePdf />
          Gerar relatório PDF
        </button>
      </div>

      <div className="estoque-card">
        <div className="estoque-card-topo">
          <h2>Produtos em estoque</h2>

          <div className="estoque-acoes">
            <button
              type="button"
              className="btn-novo"
              onClick={() => setModalNovoAberto(true)}
            >
              <FaPlus />
              Novo
            </button>
          </div>
        </div>

        <div className="estoque-filtros">
          <input
            type="text"
            placeholder="Buscar produto no estoque..."
            value={busca}
            onChange={(event) => setBusca(event.target.value)}
          />

          <div className="filtro-select">
            <label htmlFor="filtroEstoque">Estoque:</label>

            <select
              id="filtroEstoque"
              value={filtroEstoque}
              onChange={(event) => setFiltroEstoque(event.target.value)}
            >
              <option value="todos">Todos</option>
              <option value="normal">Normal</option>
              <option value="baixo">Estoque baixo</option>
              <option value="sem-estoque">Sem estoque</option>
            </select>
          </div>
        </div>

        <div className="tabela-container">
          <table className="tabela-estoque">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>Preço</th>
                <th>Estoque</th>
                <th>Situação</th>
                <th>Ações</th>
              </tr>
            </thead>

            <tbody>
              {loading && (
                <tr>
                  <td colSpan="6" className="nenhum-produto">
                    Carregando produtos...
                  </td>
                </tr>
              )}

              {!loading && erro && (
                <tr>
                  <td colSpan="6" className="nenhum-produto">
                    {erro}
                  </td>
                </tr>
              )}

              {!loading &&
                !erro &&
                produtosFiltrados.map((produto) => {
                  const estoque = Number(produto.estoque);
                  const situacao = obterSituacao(estoque);

                  return (
                    <tr
                      key={produto.id}
                      className="linha-clicavel"
                      onClick={() => abrirEdicao(produto)}
                    >
                      <td>#{produto.id}</td>

                      <td>{produto.nome}</td>

                      <td>
                        {Number(produto.preco).toLocaleString("pt-BR", {
                          style: "currency",
                          currency: "BRL",
                        })}
                      </td>

                      <td>{estoque}</td>

                      <td>
                        <span className={situacao.classe}>
                          {situacao.texto}
                        </span>
                      </td>

                      <td className="acao-coluna">
                        <button
                          type="button"
                          className="btn-excluir-produto"
                          onClick={(event) => abrirExclusao(event, produto)}
                          aria-label={`Excluir ${produto.nome}`}
                        >
                          <FaTrash />
                          Excluir
                        </button>
                      </td>
                    </tr>
                  );
                })}

              {!loading && !erro && produtosFiltrados.length === 0 && (
                <tr>
                  <td colSpan="6" className="nenhum-produto">
                    Nenhum produto encontrado.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {!loading && !erro && (
          <p className="instrucao-selecao">
            Clique em um produto para editar os dados. Use o botão
            "Excluir" para removê-lo do estoque.
          </p>
        )}
      </div>

      <ModalNovoProduto
        open={modalNovoAberto}
        onClose={() => setModalNovoAberto(false)}
        recarregarProdutos={carregarProdutos}
      />

      <ModalEditarProduto
        open={modalEditarAberto}
        produto={produtoEditando}
        onClose={() => setModalEditarAberto(false)}
        recarregarProdutos={carregarProdutos}
      />

      <ModalExcluirProduto
        open={modalExcluirAberto}
        produto={produtoExcluindo}
        onClose={() => setModalExcluirAberto(false)}
        recarregarProdutos={carregarProdutos}
      />
    </section>
  );
}

export default Estoque;
