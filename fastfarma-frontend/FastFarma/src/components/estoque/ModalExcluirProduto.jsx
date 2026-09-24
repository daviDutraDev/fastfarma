import { useState } from "react";
import {
  FaExclamationTriangle,
  FaTimes,
} from "react-icons/fa";

import {
  deletarProduto,
} from "../../services/api/Produtos";

import "./ModalExcluirProduto.css";

function ModalExcluirProduto({
  open,
  produto,
  onClose,
  recarregarProdutos,
}) {
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  if (!open || !produto) return null;

  const fecharModal = () => {
    if (loading) return;

    setErro("");
    onClose();
  };

  const confirmarExclusao = async () => {
    try {
      setLoading(true);
      setErro("");

      await deletarProduto(produto.id);

      await recarregarProdutos();

      onClose();
    } catch (error) {
      setErro(
        error.message ||
          "Não foi possível excluir o produto."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="modal-excluir-fundo"
      onMouseDown={fecharModal}
    >
      <div
        className="modal-excluir-container"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-excluir-header">
          <div>
            <h2>Excluir produto</h2>

            <p>
              Essa operação não poderá ser desfeita.
            </p>
          </div>

          <button
            type="button"
            className="modal-excluir-fechar"
            onClick={fecharModal}
            disabled={loading}
            aria-label="Fechar modal"
          >
            <FaTimes />
          </button>
        </div>

        <div className="aviso-exclusao">
          <div className="aviso-exclusao-icone">
            <FaExclamationTriangle />
          </div>

          <p>
            Você realmente deseja excluir o produto{" "}
            <strong>{produto.nome}</strong>?
          </p>
        </div>

        {erro && (
          <p className="modal-excluir-erro">
            {erro}
          </p>
        )}

        <div className="modal-excluir-botoes">
          <button
            type="button"
            className="btn-excluir-cancelar"
            onClick={fecharModal}
            disabled={loading}
          >
            Cancelar
          </button>

          <button
            type="button"
            className="btn-confirmar-exclusao"
            onClick={confirmarExclusao}
            disabled={loading}
          >
            {loading
              ? "Excluindo..."
              : "Excluir produto"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ModalExcluirProduto;