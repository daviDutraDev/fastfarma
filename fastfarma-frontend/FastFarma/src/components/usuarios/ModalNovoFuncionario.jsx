import { useState } from "react";
import { FaTimes } from "react-icons/fa";
import { CadastrarFuncionario } from "../../services/api/Usuarios";
import "./ModalNovoFuncionario.css";

/**
 * Modal de cadastro de funcionario, aberto a partir da aba
 * Usuários > Funcionários. So quem ja e FUNCIONARIO chega nessa tela
 * (a rota /painel/* e protegida), e o backend tambem exige a mesma
 * role em POST /api/usuarios/funcionarios.
 */
function ModalNovoFuncionario({ open, onClose, recarregarUsuarios }) {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [telefone, setTelefone] = useState("");
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  if (!open) return null;

  const limparFormulario = () => {
    setNome("");
    setEmail("");
    setTelefone("");
    setSenha("");
    setErro("");
  };

  const fecharModal = () => {
    if (loading) return;
    limparFormulario();
    onClose();
  };

  const enviarFormulario = async (event) => {
    event.preventDefault();

    setErro("");

    if (!nome.trim() || !email.trim() || !senha) {
      setErro("Preencha nome, email e senha.");
      return;
    }

    if (senha.length < 4) {
      setErro("A senha deve ter pelo menos 4 caracteres.");
      return;
    }

    try {
      setLoading(true);

      await CadastrarFuncionario({
        nome: nome.trim(),
        email: email.trim(),
        senha,
        telefone: telefone.trim() || undefined,
      });

      await recarregarUsuarios();

      limparFormulario();
      onClose();
    } catch (error) {
      setErro(error.message || "Erro ao cadastrar funcionário.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-fundo" onMouseDown={fecharModal}>
      <div
        className="modal-container"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div>
            <h2>Novo funcionário</h2>
            <p>Informe os dados de acesso do novo funcionário.</p>
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
            <label htmlFor="func-nome">Nome</label>

            <input
              id="func-nome"
              type="text"
              placeholder="Ex.: Maria Oliveira"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="func-email">Email</label>

            <input
              id="func-email"
              type="email"
              placeholder="funcionario@fastfarma.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="func-telefone">Telefone (opcional)</label>

            <input
              id="func-telefone"
              type="tel"
              placeholder="(47) 99999-9999"
              value={telefone}
              onChange={(event) => setTelefone(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="func-senha">Senha provisória</label>

            <input
              id="func-senha"
              type="password"
              placeholder="Mínimo 4 caracteres"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              disabled={loading}
            />
          </div>

          <p className="form-aviso">
            O funcionário poderá trocar essa senha depois, em "Meu Perfil".
          </p>

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

            <button type="submit" className="btn-confirmar" disabled={loading}>
              {loading ? "Cadastrando..." : "Cadastrar funcionário"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ModalNovoFuncionario;