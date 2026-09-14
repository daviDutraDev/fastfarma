import { useEffect, useState } from "react";
import { FaTimes } from "react-icons/fa";
import { atualizarEstoque } from "../../services/api/Estoque";
import "./ModalAjustarEstoque.css";

function ModalAjustarEstoque({
  open,
  produto,
  onClose,
  recarregarProdutos,
}) {
  const [quantidade, setQuantidade] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  useEffect(() => {
    if (open && produto) {
      setQuantidade(String(produto.estoque));
      setErro("");
    }
  }, [open, produto]);

  if (!open || !produto) return null;

  const fecharModal = () => {
    if (loading) return;

    setErro("");
    onClose();
  };

  const enviarFormulario = async (event) => {
    event.preventDefault();

    setErro("");

    const novaQuantidade = Number(quantidade);

    if (
      quantidade === "" ||
      !Number.isInteger(novaQuantidade) ||
      novaQuantidade < 0
    ) {
      setErro("Digite uma quantidade inteira válida.");
      return;
    }

    try {
      setLoading(true);

      await atualizarEstoque(produto.id, novaQuantidade);

      await recarregarProdutos();

      onClose();
    } catch (error) {
      setErro(
        error.mensagem ||
          "Não foi possível atualizar o estoque."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="modal-fundo"
      onMouseDown={fecharModal}
    >
      <div
        className="modal-container modal-pequeno"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <h2>Ajustar estoque</h2>
            <p>{produto.nome}</p>
          </div>

          <button
            type="button"
            className="modal-fechar"
            onClick={fecharModal}
            disabled={loading}
            aria-label="Fechar modal"
          >
            <FaTimes />
          </button>
        </div>

        <form onSubmit={enviarFormulario}>
          <div className="estoque-atual">
            <span>Estoque atual</span>

            <strong>
              {produto.estoque} unidades
            </strong>
          </div>

          <div className="form-group">
            <label htmlFor="quantidade">
              Nova quantidade no estoque
            </label>

            <input
              id="quantidade"
              type="number"
              min="0"
              step="1"
              value={quantidade}
              onChange={(event) =>
                setQuantidade(event.target.value)
              }
              disabled={loading}
              autoFocus
            />
          </div>

          {erro && (
            <p className="modal-erro">{erro}</p>
          )}

          <div className="modal-botoes">
            <button
              type="button"
              className="btn-cancelar"
              onClick={fecharModal}
              disabled={loading}
            >
              Cancelar
            </button>

            <button
              type="submit"
              className="btn-confirmar"
              disabled={loading}
            >
              {loading
                ? "Salvando..."
                : "Salvar estoque"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ModalAjustarEstoque;