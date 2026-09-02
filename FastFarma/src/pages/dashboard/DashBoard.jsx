import "./DashBoard.css";

function Dashboard() {
  const pedidos = [
    {
      id: 3,
      cliente: "Davi",
      status: "PRONTO",
      itens: 1,
      codigo: 5944,
    },
    {
      id: 2,
      cliente: "Davi",
      status: "REJEITADO",
      itens: 1,
      codigo: 5659,
    },
    {
      id: 1,
      cliente: "Davi",
      status: "PRONTO",
      itens: 1,
      codigo: 6465,
    },
  ];

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Dashboard</h1>

        <button className="btn-gerar-relatorio">
          Gerar Relatório PDF
        </button>
      </header>

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
                  <td>#{pedido.id}</td>

                  <td>{pedido.cliente}</td>

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

                  <td>{pedido.itens} item(s)</td>

                  <td>{pedido.codigo}</td>
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