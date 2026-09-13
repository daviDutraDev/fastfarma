import { FaExclamationTriangle, FaTimes } from "react-icons/fa";
import "./ModalEstoque.css";

function ModalExcluirProduto({
  open,
  produto,
  onClose,
  onExcluir,
}) {
  if (!open || !produto) return null;

  return (
    <div className="modal-fundo">
      <div className="modal-container modal-pequeno">
        <div className="modal-header">
          <div>
            <h2>Excluir produto</h2>
            <p>Essa operação não poderá ser desfeita.</p>
          </div>

          <button className="modal-fechar" onClick={onClose}>
            <FaTimes />
          </button>
        </div>

        <div className="aviso-exclusao">
          <FaExclamationTriangle />

          <p>
            Você realmente deseja excluir o produto{" "}
            <strong>{produto.nome}</strong>?
          </p>
        </div>

        <div className="modal-botoes">
          <button className="btn-cancelar" onClick={onClose}>
            Cancelar
          </button>

          <button className="btn-confirmar btn-confirmar-exclusao" onClick={onExcluir}>
            Excluir produto
          </button>
        </div>
      </div>
    </div>
  );
}

export default ModalExcluirProduto;