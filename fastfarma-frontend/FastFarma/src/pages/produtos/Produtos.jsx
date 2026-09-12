import { useState, useEffect } from "react";
import "./Produtos.css";
import { BuscarProdutos } from "../../services/api/Produtos.jsx";

function Produtos() {
  const [busca, setBusca] = useState("");
  const [categoria, setCategoria] = useState("todas");
  const [situacao, setSituacao] = useState("todos");
  const [produtos, setProdutos] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState(null);

  useEffect(() => {
    const fetchProdutos = async () => {
      try {
        setLoading(true);
        setError(null);
        setMensagem(null);

        const data = await BuscarProdutos();

        setProdutos(data.dados);
        console.log(data.dados)

        setMensagem(
          data.mensagem || "Produtos carregados com sucesso"
        );
      } catch (error) {
        console.error("Erro ao buscar produtos:", error);

        setError(
          error.message || "Erro ao buscar produtos"
        );
      } finally {
        setLoading(false);
      }
    };

    fetchProdutos();
  }, []);

  const categorias = [
    ...new Set(
      produtos
        .map((produto) => produto.categoria)
        .filter((c) => Boolean(c))
    ),
  ];

  const produtosFiltrados = produtos.filter((produto) => {
    const buscaValida =
      (produto.nome ?? "")
        .toLowerCase()
        .includes((busca ?? "").toLowerCase());

    const categoriaValida =
      categoria === "todas" ||
      produto.categoria === categoria;

    const disponivel = (produto.estoque ?? 0) > 0;

    const situacaoValida =
      situacao === "todos" ||
      (situacao === "disponivel" && disponivel) ||
      (situacao === "indisponivel" && !disponivel);

    return (
      buscaValida &&
      categoriaValida &&
      situacaoValida
    );
  });

  if (loading) {
    return (
      <div className="produtos-page">
        <p className="loading">
          Carregando produtos...
        </p>
      </div>
    );
  }

  return (
    <div className="produtos-page">

      {error && (
        <div className="mensagem erro">
          {error}
        </div>
      )}

      {mensagem && !error && (
        <div className="mensagem sucesso">
          {mensagem}
        </div>
      )}

      <div className="produtos-filtros">

        <input
          type="text"
          placeholder="Buscar produto por nome..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          className="produto-busca"
        />

        <div className="produto-filtro">

          <label>Categoria:</label>

          <select
            value={categoria}
            onChange={(e) =>
              setCategoria(e.target.value)
            }
          >

            <option value="todas">
              Todas
            </option>

            {categorias.map((categoria) => (
              <option
                key={categoria}
                value={categoria}
              >
                {categoria}
              </option>
            ))}

          </select>

        </div>

        <div className="produto-filtro">

          <label>Situação:</label>

          <select
            value={situacao}
            onChange={(e) =>
              setSituacao(e.target.value)
            }
          >

            <option value="todos">
              Todos
            </option>

            <option value="disponivel">
              Disponível
            </option>

            <option value="indisponivel">
              Indisponível
            </option>

          </select>

        </div>

        <span className="produto-total">
          {produtosFiltrados.length} produto(s)
        </span>

      </div>

      <div className="produtos-tabela-container">

        <table className="produtos-tabela">

          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Categoria</th>
              <th>Preço</th>
              <th>Estoque</th>
              <th>Situação</th>
            </tr>
          </thead>

          <tbody>

            {produtosFiltrados.map((produto) => {

              const disponivel =
                (produto.estoque ?? 0) > 0;

              return (
                <tr key={produto.id}>

                  <td>
                    {produto.id}
                  </td>

                  <td>
                    {produto.nome}
                  </td>

                  <td>
                    {produto.categoria || "—"}
                  </td>

                  <td>
                    {(produto.preco ?? 0).toLocaleString(
                      "pt-BR",
                      {
                        style: "currency",
                        currency: "BRL",
                      }
                    )}
                  </td>

                  <td>
                    {produto.estoque}
                  </td>

                  <td>

                    <span
                      className={
                        disponivel
                          ? "situacao disponivel"
                          : "situacao indisponivel"
                      }
                    >
                      {produto.situacao || (disponivel ? "Disponível" : "Indisponível")}
                    </span>

                  </td>

                </tr>
              );
            })}

          </tbody>

        </table>

      </div>

    </div>
  );
}

export default Produtos;