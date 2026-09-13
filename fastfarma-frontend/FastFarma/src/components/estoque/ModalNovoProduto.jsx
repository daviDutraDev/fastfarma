import { useState } from "react";
import { FaTimes } from "react-icons/fa";
import "./ModalEstoque.css";

function ModalNovoProduto({ open, onClose, onCadastrar }) {
  const [nome, setNome] = useState("");
  const [preco, setPreco] = useState("");
  const [estoque, setEstoque] = useState("");

  if (!open) return null;

  const fecharModal = () => {
    setNome("");
    setPreco("");
    setEstoque("");
    onClose();
  };

  const enviarFormulario = (event) => {
    event.preventDefault();

    if (!nome.trim() || preco === "" || estoque === "") {
      alert("Preencha todos os campos.");
      return;
    }

    if (Number(preco) <= 0) {
      alert("Informe um preço válido.");
      return;
    }

    if (Number(estoque) < 0) {
      alert("O estoque não pode ser negativo.");
      return;
    }

    onCadastrar({
      nome: nome.trim(),
      preco: Number(preco),
      estoque: Number(estoque),
    });

    setNome("");
    setPreco("");
    setEstoque("");
  };

  return (
    <div className="modal-fundo">
      <div className="modal-container">
        <div className="modal-header">
          <div>
            <h2>Novo produto</h2>
            <p>Informe os dados do produto.</p>
          </div>

          <button className="modal-fechar" onClick={fecharModal}>
            <FaTimes />
          </button>
        </div>

        <form onSubmit={enviarFormulario}>
          <div className="form-group">
            <label htmlFor="nome">Nome do produto</label>

            <input
              id="nome"
              type="text"
              placeholder="Ex.: Dipirona"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
            />
          </div>

          <div className="form-linha">
            <div className="form-group">
              <label htmlFor="preco">Preço</label>

              <input
                id="preco"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="0,00"
                value={preco}
                onChange={(event) => setPreco(event.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="estoque">Estoque inicial</label>

              <input
                id="estoque"
                type="number"
                min="0"
                step="1"
                placeholder="0"
                value={estoque}
                onChange={(event) => setEstoque(event.target.value)}
              />
            </div>
          </div>

          <div className="modal-botoes">
            <button
              type="button"
              className="btn-cancelar"
              onClick={fecharModal}
            >
              Cancelar
            </button>

            <button type="submit" className="btn-confirmar">
              Cadastrar produto
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ModalNovoProduto;