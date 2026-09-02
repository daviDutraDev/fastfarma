import { useState } from "react";
import "./Pedidos.css";

function Pedidos() {
  const [filtro, setFiltro] = useState("todos");

  const pedidos = [
    {
      id: 3,
      cliente: "davi",
      status: "PRONTO",
      itens: 1,
      codigo: 5944,
    },
    {
      id: 2,
      cliente: "davi",
      status: "REJEITADO",
      itens: 1,
      codigo: 5659,
    },
    {
      id: 1,
      cliente: "davi",
      status: "PRONTO",
      itens: 1,
      codigo: 6465,
    },
  ];

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

  const gerarRelatorio = () => {
    console.log("Gerar relatório");
  };

  return (
    <div className="pedidos">
      <div className="pedidos-header">
        <h1>Pedidos</h1>

        <button
          className="btn-relatorio"
          onClick={gerarRelatorio}
        >
          Gerar Relatório PDF
        </button>
      </div>

      <div className="pedidos-content">
        <div className="pedidos-filtro">
          <label htmlFor="filtro">Filtrar:</label>

          <select
            id="filtro"
            value={filtro}
            onChange={(e) => setFiltro(e.target.value)}
          >
            <option value="todos">Todos</option>
            <option value="pronto">Pronto</option>
            <option value="rejeitado">Rejeitado</option>
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
                  <td>#{pedido.id}</td>

                  <td>{pedido.cliente}</td>

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

                  <td>{pedido.codigo}</td>

                  <td>
                    <button
                      className="btn-ver"
                      onClick={() => verPedido(pedido)}
                    >
                      Ver
                    </button>
                  </td>

                  <td>
                    <button
                      className="btn-analisar"
                      onClick={() => analisarPedido(pedido)}
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