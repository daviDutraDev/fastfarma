import { useState } from "react";
import { FaTimes } from "react-icons/fa";
import { cadastrarProduto } from "../../services/api/Produtos";
import "./ModalNovoProduto.css";

function ModalNovoProduto({ open, onClose, recarregarProdutos }) {
  const [nome, setNome] = useState("");
  const [preco, setPreco] = useState("");
  const [estoque, setEstoque] = useState("");
  const [categoria, setCategoria] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  if (!open) return null;

  const limparFormulario = () => {
    setNome("");
    setPreco("");
    setEstoque("");
    setCategoria("");
    setErro("");
  };

  const fecharModal = () => {
    limparFormulario();
    onClose();
  };

  const enviarFormulario = async (event) => {
    event.preventDefault();

    setErro("");

    if (
      !nome.trim() ||
      preco === "" ||
      estoque === "" ||
      !categoria.trim()
    ) {
      setErro("Preencha todos os campos.");
      return;
    }

    if (Number(preco) <= 0) {
      setErro("Informe um preço válido.");
      return;
    }

    if (Number(estoque) < 0) {
      setErro("O estoque não pode ser negativo.");
      return;
    }

    try {
      setLoading(true);

      await cadastrarProduto({
        nome: nome.trim(),
        preco: Number(preco),
        estoque: Number(estoque),
        categoria: categoria.trim(),
      });

      await recarregarProdutos();

      limparFormulario();
      onClose();
    } catch (error) {
      setErro(error.message || "Erro ao cadastrar produto.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-fundo">
      <div className="modal-container">
        <div className="modal-header">
          <div>
            <h2>Novo produto</h2>
            <p>Informe os dados do produto.</p>
          </div>

          <button
            type="button"
            className="modal-fechar"
            onClick={fecharModal}
            disabled={loading}
          >
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
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="categoria">Categoria</label>

            <input
              id="categoria"
              type="text"
              placeholder="Ex.: Analgésico"
              value={categoria}
              onChange={(event) => setCategoria(event.target.value)}
              disabled={loading}
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
                disabled={loading}
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
                disabled={loading}
              />
            </div>
          </div>

          {erro && <p className="modal-erro">{erro}</p>}

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
              {loading ? "Cadastrando..." : "Cadastrar produto"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ModalNovoProduto;