import { useState, useEffect, useCallback } from "react";
import { BuscarUsuarios, ExcluirUsuario } from "../../services/api/Usuarios";
import ModalNovoFuncionario from "../../components/usuarios/ModalNovoFuncionario";
import "./Usuario.css";

/**
 * Tabela de usuarios de um unico tipo (CLIENTE ou FUNCIONARIO).
 * As paginas "Clientes" e "Funcionarios" sao so duas instancias
 * deste componente, uma para cada aba — a separacao que antes era
 * so uma coluna "Tipo" numa lista unica agora e a propria navegacao.
 *
 * `permiteCriar`: so a aba Funcionarios passa true — e la que aparece
 * o botao "Novo funcionario" (cliente se cadastra sozinho, pela tela
 * publica de cadastro).
 */
function Usuario({ tipo, titulo, permiteCriar = false }) {
  const [usuarios, setUsuarios] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState(null);

  // Guarda o id do usuário sendo excluído no momento, só para
  // desabilitar o botão daquela linha e evitar clique duplo — as
  // outras linhas continuam normais enquanto isso.
  const [excluindoId, setExcluindoId] = useState(null);

  const [modalNovoAberto, setModalNovoAberto] = useState(false);

  const fetchUsuarios = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      setMensagem(null);

      const data = await BuscarUsuarios();

      const filtrados = (data.dados || []).filter(
        (usuario) => usuario.tipo === tipo
      );
      setUsuarios(filtrados);

      setMensagem(
        data.mensagem || "Usuários carregados com sucesso"
      );
    } catch (error) {
      console.error("Erro ao buscar usuários:", error);

      setError(
        error.message || "Erro ao buscar usuários"
      );
    } finally {
      setLoading(false);
    }
  }, [tipo]);

  useEffect(() => {
    fetchUsuarios();
  }, [fetchUsuarios]);

  const excluirUsuario = async (usuario) => {
    const desejaExcluir = window.confirm(
      `Você realmente deseja excluir o usuário "${usuario.nome}"? Essa ação não pode ser desfeita.`
    );
    if (!desejaExcluir) return;

    try {
      setExcluindoId(usuario.id);
      setError(null);

      await ExcluirUsuario(usuario.id);

      setUsuarios((lista) =>
        lista.filter((item) => item.id !== usuario.id)
      );
      setMensagem(`Usuário "${usuario.nome}" excluído com sucesso`);
    } catch (error) {
      console.error("Erro ao excluir usuário:", error);

      // Ex.: o backend recusa excluir o admin padrão (id=1) — a
      // mensagem de erro real da API aparece aqui.
      setError(error.message || "Erro ao excluir usuário");
    } finally {
      setExcluindoId(null);
    }
  };


  if (loading) {
    return (
      <div className="usuarios-page">
        <p className="loading">
          Carregando {titulo.toLowerCase()}...
        </p>
      </div>
    );
  }

  return (
    <div className="usuarios-page">

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

      <div className="usuarios-card">

        <div className="usuarios-card-topo">
          <h2>{titulo}</h2>

          {permiteCriar && (
            <button
              type="button"
              className="btn-novo-funcionario"
              onClick={() => setModalNovoAberto(true)}
            >
              + Novo funcionário
            </button>
          )}
        </div>

        <table className="usuarios-tabela">

          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Email</th>
              <th>Ação</th>
            </tr>
          </thead>

          <tbody>

            {usuarios.map((usuario) => (
              <tr key={usuario.id}>

                <td>
                  {usuario.id}
                </td>

                <td>
                  {usuario.nome}
                </td>

                <td>
                  {usuario.email}
                </td>

                <td className="acao-coluna">

                  <button
                    className="btn-excluir"
                    onClick={() => excluirUsuario(usuario)}
                    disabled={excluindoId === usuario.id}
                  >
                    {excluindoId === usuario.id ? "Excluindo..." : "Excluir"}
                  </button>

                </td>

              </tr>
            ))}

            {usuarios.length === 0 && (
              <tr>
                <td colSpan="4" className="tabela-vazia">
                  Nenhum {titulo.toLowerCase().replace(/s$/, "")} encontrado.
                </td>
              </tr>
            )}

          </tbody>

        </table>

      </div>

      {permiteCriar && (
        <ModalNovoFuncionario
          open={modalNovoAberto}
          onClose={() => setModalNovoAberto(false)}
          recarregarUsuarios={fetchUsuarios}
        />
      )}

    </div>
  );
}

export default Usuario;