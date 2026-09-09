import { useState } from "react";
import "./Produtos.css";

function Produtos() {
  const [busca, setBusca] = useState("");
  const [categoria, setCategoria] = useState("todas");
  const [situacao, setSituacao] = useState("todos");

  // SIMULANDO OS DADOS QUE DEPOIS VIRÃO DO BACK-END
  const produtos = [
    {
      id: 1,
      nome: "Dipirona",
      categoria: "Analgésico",
      preco: 10.5,
      estoque: 19,
    },
    {
      id: 2,
      nome: "Paracetamol",
      categoria: "Analgésico",
      preco: 8,
      estoque: 20,
    },
    {
      id: 3,
      nome: "Vitamina C",
      categoria: "Vitamina",
      preco: 15,
      estoque: 20,
    },
    {
      id: 4,
      nome: "Amoxicilina",
      categoria: "Antibiótico",
      preco: 24.9,
      estoque: 0,
    },
  ];

  const categorias = [
    ...new Set(
      produtos.map((produto) => produto.categoria)
    ),
  ];

  const produtosFiltrados = produtos.filter((produto) => {
    const buscaValida =
      produto.nome
        .toLowerCase()
        .includes(busca.toLowerCase());

    const categoriaValida =
      categoria === "todas" ||
      produto.categoria === categoria;

    const disponivel = produto.estoque > 0;

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

  return (
    <div className="produtos-page">

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
                produto.estoque > 0;

              return (
                <tr key={produto.id}>

                  <td>
                    {produto.id}
                  </td>

                  <td>
                    {produto.nome}
                  </td>

                  <td>
                    {produto.categoria}
                  </td>

                  <td>
                    {produto.preco.toLocaleString(
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

                      {disponivel
                        ? "Disponível"
                        : "Indisponível"}

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