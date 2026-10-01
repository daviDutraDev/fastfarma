import { useEffect, useMemo, useState } from "react";
import { FaTimes, FaMinus, FaPlus } from "react-icons/fa";
import { AtualizarProduto } from "../../services/api/Produtos";

import "./ModalEditarProduto.css";

function obterSituacao(quantidade) {
  if (quantidade === 0) {
    return { texto: "Sem estoque", classe: "sem-estoque" };
  }
  if (quantidade <= 5) {
    return { texto: "Estoque baixo", classe: "baixo" };
  }
  return { texto: "Normal", classe: "normal" };
}

/**
 * Modal de edicao de um produto ja existente. Abre ao clicar na linha
 * do produto na tabela de estoque. Deixa editar nome, categoria e
 * preco normalmente; o estoque ganha um controle proprio (numero
 * grande + botoes -/+), por ser a acao mais comum nesta tela.
 */
function ModalEditarProduto({ open, produto, onClose, recarregarProdutos }) {
  const [nome, setNome] = useState("");
  const [preco, setPreco] = useState("");
  const [estoque, setEstoque] = useState(0);
  const [categoria, setCategoria] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  // Preenche o formulario com os dados do produto sempre que o modal
  // abrir (ou trocar de produto), para nao herdar valores da edicao
  // anterior nem exigir remount do componente.
  useEffect(() => {
    if (open && produto) {
      setNome(produto.nome ?? "");
      setPreco(produto.preco ?? "");
      setEstoque(Number(produto.estoque) || 0);
      setCategoria(produto.categoria ?? "");
      setErro("");
    }
  }, [open, produto]);

  const situacao = useMemo(() => obterSituacao(estoque), [estoque]);

  if (!open || !produto) return null;

  const fecharModal = () => {
    if (loading) return;
    setErro("");
    onClose();
  };

  const ajustarEstoque = (delta) => {
    setEstoque((atual) => Math.max(0, atual + delta));
  };

  const enviarFormulario = async (event) => {
    event.preventDefault();

    setErro("");

    if (!nome.trim() || preco === "" || !categoria.trim()) {
      setErro("Preencha todos os campos.");
      return;
    }

    if (Number(preco) <= 0) {
      setErro("Informe um preço válido.");
      return;
    }

    try {
      setLoading(true);

      await AtualizarProduto(produto.id, {
        nome: nome.trim(),
        preco: Number(preco),
        estoque: Number(estoque),
        categoria: categoria.trim(),
      });

      await recarregarProdutos();
      onClose();
    } catch (error) {
      setErro(error.message || "Erro ao salvar as alterações.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="editar-produto-fundo" onMouseDown={fecharModal}>
      <div
        className="editar-produto-container"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="editar-produto-header">
          <div>
            <h2>Editar produto</h2>
            <p>#{produto.id} — {produto.nome}</p>
          </div>

          <button
            type="button"
            className="editar-produto-fechar"
            onClick={fecharModal}
            disabled={loading}
            aria-label="Fechar"
          >
            <FaTimes />
          </button>
        </div>

        <form onSubmit={enviarFormulario}>
          <div className="campo">
            <label htmlFor="editar-nome">Nome do produto</label>
            <input
              id="editar-nome"
              type="text"
              placeholder="Ex.: Dipirona"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="campo-linha">
            <div className="campo">
              <label htmlFor="editar-categoria">Categoria</label>
              <input
                id="editar-categoria"
                type="text"
                placeholder="Ex.: Analgésico"
                value={categoria}
                onChange={(event) => setCategoria(event.target.value)}
                disabled={loading}
              />
            </div>

            <div className="campo">
              <label htmlFor="editar-preco">Preço</label>
              <input
                id="editar-preco"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="0,00"
                value={preco}
                onChange={(event) => setPreco(event.target.value)}
                disabled={loading}
              />
            </div>
          </div>

          <div className="campo campo-estoque">
            <div className="campo-estoque-topo">
              <label htmlFor="editar-estoque-input">Estoque</label>
              <span className={`selo-situacao situacao-${situacao.classe}`}>
                {situacao.texto}
              </span>
            </div>

            <div className="painel-estoque-controle">
              <button
                type="button"
                className="passo-estoque"
                onClick={() => ajustarEstoque(-1)}
                disabled={loading || estoque <= 0}
                aria-label="Diminuir uma unidade"
              >
                <FaMinus />
              </button>

              <input
                id="editar-estoque-input"
                className="numero-estoque"
                type="number"
                min="0"
                step="1"
                value={estoque}
                onChange={(event) =>
                  setEstoque(Math.max(0, Number(event.target.value) || 0))
                }
                disabled={loading}
                aria-label="Quantidade em estoque"
              />

              <button
                type="button"
                className="passo-estoque"
                onClick={() => ajustarEstoque(1)}
                disabled={loading}
                aria-label="Adicionar uma unidade"
              >
                <FaPlus />
              </button>
            </div>
          </div>

          {erro && <p className="editar-produto-erro">{erro}</p>}

          <div className="editar-produto-botoes">
            <button
              type="button"
              className="btn-cancelar-edicao"
              onClick={fecharModal}
              disabled={loading}
            >
              Cancelar
            </button>

            <button type="submit" className="btn-salvar-edicao" disabled={loading}>
              {loading ? "Salvando..." : "Salvar alterações"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ModalEditarProduto;
