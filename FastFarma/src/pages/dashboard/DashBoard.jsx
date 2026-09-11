import "./DashBoard.css";
import { useEffect, useState } from "react";
import { BuscarPedidos } from "../../services/api/Pedidos";

function Dashboard() {
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

        setMensagem(data.mensagem || "Pedidos carregados com sucesso");
      } catch (error) {
        console.error("Erro ao buscar pedidos:", error);

        setError(error.message || "Erro ao buscar pedidos");
      } finally {
        setLoading(false);
      }
    };

    fetchPedidos();
  }, []);

  if (loading) {
    return (
      <div className="dashboard">
        <p className="loading">
          Carregando pedidos...
        </p>
      </div>
    );
  }

  return (
    <div className="dashboard">

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

      <div className="dashboard-content">

        <section className="cards-container">

          <div className="dashboard-card total">
            <span>Total de pedidos</span>
            <strong>{pedidos.length}</strong>
          </div>

          <div className="dashboard-card pendentes">
            <span>Pendentes</span>
            <strong>0</strong>
          </div>

          <div className="dashboard-card receita">
            <span>Receita total</span>
            <strong>R$ 21,00</strong>
          </div>

          <div className="dashboard-card estoque">
            <span>Estoque baixo</span>
            <strong>0</strong>
          </div>

        </section>

        <section className="pedidos-box">

          <div className="pedidos-box-header">
            <h2>Pedidos recentes</h2>
            <span>{pedidos.length} total</span>
          </div>

          <table className="pedidos-table">

            <thead>
              <tr>
                <th>ID</th>
                <th>Cliente</th>
                <th>Status</th>
                <th>Itens</th>
                <th>Código</th>
              </tr>
            </thead>

            <tbody>
              {pedidos.map((pedido) => (
                <tr key={pedido.id}>

                  <td>
                    #{pedido.id}
                  </td>

                  <td>
                    {pedido.cliente}
                  </td>

                  <td>
                    <span
                      className={`status ${
                        pedido.status === "PRONTO"
                          ? "status-pronto"
                          : pedido.status === "REJEITADO"
                          ? "status-rejeitado"
                          : "status-pendente"
                      }`}
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

                </tr>
              ))}
            </tbody>

          </table>

        </section>

      </div>
    </div>
  );
}

export default Dashboard;