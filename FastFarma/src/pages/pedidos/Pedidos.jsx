import { useState, useEffect } from "react";
import "./Pedidos.css";
import { BuscarPedidos } from "../../services/api/Pedidos.jsx";

function Pedidos() {
  const [filtro, setFiltro] = useState("todos");
  const [pedidos, setPedidos] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState(null);

  useEffect(() => {
    const fetchPedidos = async () => {
      try {
        setLoading(true);
        setError(null);
        setMensagem(null);

        const data = await BuscarPedidos();

        setPedidos(data.dados);

        setMensagem(
          data.mensagem || "Pedidos carregados com sucesso"
        );
      } catch (error) {
        console.error("Erro ao buscar pedidos:", error);

        setError(
          error.message || "Erro ao buscar pedidos"
        );
      } finally {
        setLoading(false);
      }
    };

    fetchPedidos();
  }, []);

  const pedidosFiltrados = pedidos.filter((pedido) => {
    if (filtro === "todos") {
      return true;
    }

    return pedido.status.toLowerCase() === filtro;
  });

  const verPedido = (pedido) => {
    console.log("Ver pedido:", pedido);
  };

  const analisarPedido = (pedido) => {
    console.log("Analisar pedido:", pedido);
  };

  if (loading) {
    return (
      <div className="pedidos">
        <p className="loading">
          Carregando pedidos...
        </p>
      </div>
    );
  }

  return (
    <div className="pedidos">

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

      <div className="pedidos-content">

        <div className="pedidos-filtro">

          <label htmlFor="filtro">
            Filtrar:
          </label>

          <select
            id="filtro"
            value={filtro}
            onChange={(e) =>
              setFiltro(e.target.value)
            }
          >
            <option value="todos">
              Todos
            </option>

            <option value="pronto">
              Pronto
            </option>

            <option value="rejeitado">
              Rejeitado
            </option>
          </select>

          <span>
            (clique em uma linha para ver os detalhes do pedido)
          </span>

        </div>

        <div className="tabela-container">

          <table className="tabela-pedidos">

            <thead>
              <tr>
                <th>ID</th>
                <th>Cliente</th>
                <th>Status</th>
                <th>Itens</th>
                <th>Código</th>
                <th>Ver</th>
                <th>Analisar</th>
              </tr>
            </thead>

            <tbody>

              {pedidosFiltrados.map((pedido) => (
                <tr key={pedido.id}>

                  <td>
                    #{pedido.id}
                  </td>

                  <td>
                    {pedido.cliente}
                  </td>

                  <td>
                    <span
                      className={`status ${pedido.status.toLowerCase()}`}
                    >
                      {pedido.status}
                    </span>
                  </td>

                  <td>
                    {pedido.itens} item(s)
                  </td>

                  <td>
                    {pedido.codigo}
                  </td>

                  <td>
                    <button
                      className="btn-ver"
                      onClick={() =>
                        verPedido(pedido)
                      }
                    >
                      Ver
                    </button>
                  </td>

                  <td>
                    <button
                      className="btn-analisar"
                      onClick={() =>
                        analisarPedido(pedido)
                      }
                    >
                      Analisar
                    </button>
                  </td>

                </tr>
              ))}

            </tbody>

          </table>

        </div>

      </div>

    </div>
  );
}

export default Pedidos;