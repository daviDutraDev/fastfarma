import { useState, useEffect } from "react";
import "./Pedidos.css";
import {
  BuscarPedidos,
  AtualizarStatusPedido,
} from "../../services/api/Pedidos.jsx";

// Transições permitidas no painel. O backend aceita qualquer troca, mas
// só REJEITADO devolve estoque, então reabrir um pedido rejeitado ou
// alterar um pronto deixaria o estoque inconsistente.
const ACOES_POR_STATUS = {
  PENDENTE: [
    { status: "APROVADO", label: "Aprovar", classe: "aprovar" },
    { status: "REJEITADO", label: "Rejeitar", classe: "rejeitar" },
  ],
  APROVADO: [
    { status: "PRONTO", label: "Marcar como pronto", classe: "pronto" },
    { status: "REJEITADO", label: "Rejeitar", classe: "rejeitar" },
  ],
  PRONTO: [],
  REJEITADO: [],
};

const formatarMoeda = (v) =>
  Number(v ?? 0).toLocaleString("pt-BR", {
    style: "currency",
    currency: "BRL",
  });

const formatarData = (iso) => {
  if (!iso) return "-";
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? "-" : d.toLocaleString("pt-BR");
};

function Pedidos() {
  const [filtro, setFiltro] = useState("todos");
  const [pedidos, setPedidos] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState(null);

  // Modal: { pedido, modo: "ver" | "analisar" } ou null
  const [modal, setModal] = useState(null);
  const [salvando, setSalvando] = useState(false);
  const [erroModal, setErroModal] = useState(null);

  useEffect(() => {
    const fetchPedidos = async () => {
      try {
        setLoading(true);
        setError(null);
        setMensagem(null);

        const data = await BuscarPedidos();

        setPedidos(Array.isArray(data?.dados) ? data.dados : []);

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

  // Fecha o modal com ESC (exceto enquanto salva)
  useEffect(() => {
    if (!modal) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape" && !salvando) fecharModal();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [modal, salvando]);

  const pedidosFiltrados = pedidos.filter((pedido) => {
    if (filtro === "todos") {
      return true;
    }

    return pedido.status.toLowerCase() === filtro;
  });

  const abrirModal = (pedido, modo) => {
    setErroModal(null);
    setModal({ pedido, modo });
  };

  const fecharModal = () => {
    if (salvando) return;
    setModal(null);
    setErroModal(null);
  };

  const verPedido = (pedido) => abrirModal(pedido, "ver");
  const analisarPedido = (pedido) => abrirModal(pedido, "analisar");

  const mudarStatus = async (pedido, acao) => {
    if (
      acao.status === "REJEITADO" &&
      !window.confirm(
        `Rejeitar o pedido #${pedido.id}? O estoque dos itens será devolvido.`
      )
    ) {
      return;
    }

    try {
      setSalvando(true);
      setErroModal(null);

      const resp = await AtualizarStatusPedido(pedido.id, acao.status);
      const atualizado = resp?.dados;

      setPedidos((lista) =>
        lista.map((p) => (p.id === pedido.id ? atualizado ?? p : p))
      );
      setError(null);
      setMensagem(resp?.mensagem || `Pedido #${pedido.id} atualizado`);
      setModal(null);
    } catch (e) {
      setErroModal(e.message || "Erro ao atualizar o pedido.");
    } finally {
      setSalvando(false);
    }
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

  const pedidoModal = modal?.pedido;
  const acoes = pedidoModal ? ACOES_POR_STATUS[pedidoModal.status] ?? [] : [];

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

            <option value="pendente">
              Pendente
            </option>

            <option value="aprovado">
              Aprovado
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
                <tr
                  key={pedido.id}
                  className="linha-clicavel"
                  onClick={() => verPedido(pedido)}
                >

                  <td>
                    #{pedido.id}
                  </td>

                  <td>
                    {pedido.criadoPor}
                  </td>

                  <td>
                    <span
                      className={`status ${pedido.status.toLowerCase()}`}
                    >
                      {pedido.status}
                    </span>
                  </td>

                  <td>
                    {pedido.itens?.length ?? 0} item(s)
                  </td>

                  <td>
                    {pedido.codigoVerificacao}
                  </td>

                  <td>
                    <button
                      className="btn-ver"
                      onClick={(e) => {
                        e.stopPropagation();
                        verPedido(pedido);
                      }}
                    >
                      Ver
                    </button>
                  </td>

                  <td>
                    <button
                      className="btn-analisar"
                      onClick={(e) => {
                        e.stopPropagation();
                        analisarPedido(pedido);
                      }}
                    >
                      Analisar
                    </button>
                  </td>

                </tr>
              ))}

              {pedidosFiltrados.length === 0 && (
                <tr>
                  <td colSpan="7" className="tabela-vazia">
                    Nenhum pedido encontrado.
                  </td>
                </tr>
              )}

            </tbody>

          </table>

        </div>

      </div>

      {pedidoModal && (
        <div className="modal-pedido-fundo" onClick={fecharModal}>
          <div
            className="modal-pedido-container"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-pedido-header">
              <div>
                <h2>
                  {modal.modo === "analisar" ? "Analisar" : "Pedido"} #{pedidoModal.id}
                </h2>
                <p>Cliente: {pedidoModal.criadoPor}</p>
              </div>

              <button
                type="button"
                className="modal-pedido-fechar"
                onClick={fecharModal}
                disabled={salvando}
                aria-label="Fechar"
              >
                ×
              </button>
            </div>

            <div className="modal-pedido-info">
              <div>
                <span>Status</span>
                <strong
                  className={`badge-status ${pedidoModal.status.toLowerCase()}`}
                >
                  {pedidoModal.status}
                </strong>
              </div>
              <div>
                <span>Código de retirada</span>
                <strong>{pedidoModal.codigoVerificacao}</strong>
              </div>
              <div>
                <span>Criado em</span>
                <strong>{formatarData(pedidoModal.criadoEm)}</strong>
              </div>
              <div>
                <span>Atualizado em</span>
                <strong>{formatarData(pedidoModal.atualizadoEm)}</strong>
              </div>
            </div>

            <table className="modal-pedido-itens">
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Preço</th>
                </tr>
              </thead>
              <tbody>
                {(pedidoModal.itens ?? []).map((item) => (
                  <tr key={item.produtoId}>
                    <td>{item.nome}</td>
                    <td>{formatarMoeda(item.precoUnitario)}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <td>Total</td>
                  <td>{formatarMoeda(pedidoModal.valorTotal)}</td>
                </tr>
              </tfoot>
            </table>

            {erroModal && (
              <div className="modal-pedido-erro">{erroModal}</div>
            )}

            {modal.modo === "analisar" && acoes.length === 0 && (
              <p className="modal-pedido-aviso">
                Este pedido já está {pedidoModal.status.toLowerCase()} e não
                pode mais ser alterado.
              </p>
            )}

            <div className="modal-pedido-botoes">
              <button
                type="button"
                className="botao-pedido-fechar"
                onClick={fecharModal}
                disabled={salvando}
              >
                Fechar
              </button>

              {modal.modo === "ver" && acoes.length > 0 && (
                <button
                  type="button"
                  className="botao-pedido-acao analisar"
                  onClick={() => setModal({ pedido: pedidoModal, modo: "analisar" })}
                >
                  Analisar
                </button>
              )}

              {modal.modo === "analisar" &&
                acoes.map((acao) => (
                  <button
                    key={acao.status}
                    type="button"
                    className={`botao-pedido-acao ${acao.classe}`}
                    onClick={() => mudarStatus(pedidoModal, acao)}
                    disabled={salvando}
                  >
                    {salvando ? "Salvando..." : acao.label}
                  </button>
                ))}
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Pedidos;