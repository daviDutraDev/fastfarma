import { useEffect, useRef, useState } from "react";
import { FiEye, FiEyeOff, FiInfo, FiUserPlus, FiX } from "react-icons/fi";
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
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const dialogRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;
    const dialog = dialogRef.current;
    dialog.showModal();
    const overflowAnterior = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      dialog.close();
      document.body.style.overflow = overflowAnterior;
    };
  }, [open]);

  if (!open) return null;

  const limparFormulario = () => {
    setNome("");
    setEmail("");
    setTelefone("");
    setSenha("");
    setErro("");
    setMostrarSenha(false);
  };

  const fecharModal = () => {
    if (loading) return;
    limparFormulario();
    onClose();
  };

  const enviarFormulario = async (event) => {
    event.preventDefault();
    if (loading) return;

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
    <dialog
      ref={dialogRef}
      className="funcionario-modal"
      aria-labelledby="funcionario-modal-titulo"
      aria-describedby="funcionario-modal-descricao"
      onCancel={(event) => { event.preventDefault(); fecharModal(); }}
      onMouseDown={(event) => {
        if (event.target !== event.currentTarget) return;
        const limites = event.currentTarget.getBoundingClientRect();
        if (event.clientX < limites.left || event.clientX > limites.right ||
            event.clientY < limites.top || event.clientY > limites.bottom) fecharModal();
      }}
    >
        <div className="funcionario-modal-header">
          <span className="funcionario-modal-icone" aria-hidden="true"><FiUserPlus /></span>
          <div>
            <h2 id="funcionario-modal-titulo">Novo funcionário</h2>
            <p id="funcionario-modal-descricao">Informe os dados de acesso do novo funcionário.</p>
          </div>

          <button
            type="button"
            className="funcionario-modal-fechar"
            aria-label="Fechar cadastro de funcionário"
            onClick={fecharModal}
            disabled={loading}
          >
            <FiX aria-hidden="true" />
          </button>
        </div>

        <form className="funcionario-modal-form" onSubmit={enviarFormulario}>
          <div className="funcionario-modal-campos">
          <div className="funcionario-modal-campo">
            <label htmlFor="func-nome">Nome completo</label>

            <input
              id="func-nome"
              type="text"
              autoComplete="name"
              autoFocus
              placeholder="Ex.: Maria Oliveira"
              value={nome}
              onChange={(event) => setNome(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="funcionario-modal-campo">
            <label htmlFor="func-email">E-mail</label>

            <input
              id="func-email"
              type="email"
              placeholder="nome@exemplo.com"
              autoComplete="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="funcionario-modal-campo">
            <label htmlFor="func-telefone">Telefone <span>(opcional)</span></label>

            <input
              id="func-telefone"
              type="tel"
              placeholder="(00) 00000-0000"
              autoComplete="tel-national"
              value={telefone}
              onChange={(event) => setTelefone(event.target.value)}
              disabled={loading}
            />
          </div>

          <div className="funcionario-modal-campo">
            <label htmlFor="func-senha">Senha provisória</label>

            <div className="funcionario-modal-senha">
            <input
              id="func-senha"
              type={mostrarSenha ? "text" : "password"}
              autoComplete="new-password"
              placeholder="Mínimo 4 caracteres"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              disabled={loading}
            />
            <button type="button" className="funcionario-modal-revelar"
              aria-label={mostrarSenha ? "Ocultar senha provisória" : "Mostrar senha provisória"}
              aria-pressed={mostrarSenha} aria-controls="func-senha" disabled={loading}
              onClick={() => setMostrarSenha(!mostrarSenha)}>
              {mostrarSenha ? <FiEyeOff aria-hidden="true" /> : <FiEye aria-hidden="true" />}
            </button>
            </div>
          </div>

          <p className="funcionario-modal-aviso">
            <FiInfo aria-hidden="true" />
            <span>O funcionário poderá trocar essa senha depois, em Meu perfil.</span>
          </p>

          {erro && <p className="funcionario-modal-erro" role="alert">{erro}</p>}
          </div>

          <div className="funcionario-modal-botoes">
            <button
              type="button"
              className="funcionario-modal-cancelar"
              onClick={fecharModal}
              disabled={loading}
            >
              Cancelar
            </button>

            <button type="submit" className="funcionario-modal-confirmar" disabled={loading}>
              {loading ? "Cadastrando..." : "Cadastrar funcionário"}
            </button>
          </div>
        </form>
    </dialog>
  );
}

export default ModalNovoFuncionario;
