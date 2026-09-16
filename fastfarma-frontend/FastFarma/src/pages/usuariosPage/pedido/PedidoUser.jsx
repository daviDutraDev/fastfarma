import "./PedidoUser.css";
import { useEffect, useState } from "react";
import { BuscarPedidosUsuario } from "../../../services/api/Pedidos";

function PedidoUser() {
  const [pedidos, setPedidos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    const carregarPedidos = async () => {
      try {
        setLoading(true);
        setError(null);
        setMensagem("");

        const usuarioSalvo = localStorage.getItem("usuario");

        if (!usuarioSalvo) {
          throw new Error(
            "Usuário não encontrado. Faça login novamente."
          );
        }

        const usuario = JSON.parse(usuarioSalvo);

        if (!usuario.nome) {
          throw new Error(
            "O nome do usuário não foi encontrado."
          );
        }

        const data = await BuscarPedidosUsuario(usuario.nome);

        setPedidos(data.dados || []);

        setMensagem(
          data.mensagem || "Pedidos carregados com sucesso."
        );
      } catch (error) {
        console.error("Erro ao buscar pedidos:", error);

        setError(
          error.message ||
            "Não foi possível carregar seus pedidos."
        );

        setPedidos([]);
        setMensagem("");
      } finally {
        setLoading(false);
      }
    };

    carregarPedidos();
  }, []);

  const formatarMoeda = (valor) => {
    return Number(valor).toLocaleString("pt-BR", {
      style: "currency",
      currency: "BRL",
    });
  };

  const formatarQuantidade = (quantidade) => {
    const total = Number(quantidade) || 0;

    return `${total} ${total === 1 ? "item" : "itens"}`;
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
          </div>

          <span className="quantidade-pedidos">
            {pedidos.length}{" "}
            {pedidos.length === 1 ? "pedido" : "pedidos"}
          </span>
        </div>

        {mensagem && (
          <div className="mensagem-pedidos">
            {mensagem}
          </div>
        )}

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
                  <tr key={pedido.id}>
                    <td className="pedido-id">
                      #{pedido.id}
                    </td>

                    <td>
                      <span
                        className={`status-pedido ${
                          pedido.status?.toLowerCase() ||
                          "pendente"
                        }`}
                      >
                        {pedido.status || "PENDENTE"}
                      </span>
                    </td>

                    <td>
                      {formatarQuantidade(
                        pedido.quantidadeItens
                      )}
                    </td>

                    <td className="pedido-valor">
                      {formatarMoeda(pedido.valor)}
                    </td>

                    <td>
                      <strong className="codigo-retirada">
                        {pedido.codigoRetirada ||
                          "Aguardando"}
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
              Quando você fizer um pedido, ele aparecerá
              aqui.
            </p>
          </div>
        )}
      </div>
    </section>
  );
}

export default PedidoUser;