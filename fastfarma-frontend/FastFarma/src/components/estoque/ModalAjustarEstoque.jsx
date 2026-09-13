import { useEffect, useState } from "react";
import { FaTimes } from "react-icons/fa";
import "./ModalEstoque.css";

function ModalAjustarEstoque({
  open,
  produto,
  onClose,
  onAjustar,
}) {
  const [quantidade, setQuantidade] = useState("");

  useEffect(() => {
    if (produto) {
      setQuantidade(produto.estoque);
    }
  }, [produto]);

  if (!open || !produto) return null;

  const enviarFormulario = (event) => {
    event.preventDefault();

    const novaQuantidade = Number(quantidade);

    if (
      quantidade === "" ||
      !Number.isInteger(novaQuantidade) ||
      novaQuantidade < 0
    ) {
      alert("Digite uma quantidade inteira válida.");
      return;
    }

    onAjustar(novaQuantidade);
  };

  return (
    <div className="modal-fundo">
      <div className="modal-container modal-pequeno">
        <div className="modal-header">
          <div>
            <h2>Ajustar estoque</h2>
            <p>{produto.nome}</p>
          </div>

          <button className="modal-fechar" onClick={onClose}>
            <FaTimes />
          </button>
        </div>

        <form onSubmit={enviarFormulario}>
          <div className="estoque-atual">
            Estoque atual:
            <strong>{produto.estoque} unidades</strong>
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
            />
          </div>

          <div className="modal-botoes">
            <button
              type="button"
              className="btn-cancelar"
              onClick={onClose}
            >
              Cancelar
            </button>

            <button type="submit" className="btn-confirmar">
              Salvar estoque
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ModalAjustarEstoque;