import { useState, useEffect } from "react";
import { BuscarUsuarios, ExcluirUsuario } from "../../services/api/Usuarios";
import "./Usuario.css";

function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState(null);
  const [excluindoId, setExcluindoId] = useState(null);

  useEffect(() => {
    const fetchUsuarios = async () => {
      try {
        setLoading(true);
        setError(null);
        setMensagem(null);

        const data = await BuscarUsuarios();

        setUsuarios(Array.isArray(data?.dados) ? data.dados : []);

        setMensagem(
          data?.mensagem || "Usuários carregados com sucesso"
        );
      } catch (error) {
        console.error("Erro ao buscar usuários:", error);

        setError(
          error.message || "Erro ao buscar usuários"
        );
      } finally {
        setLoading(false);
      }
    };

    fetchUsuarios();
  }, []);

  const excluirUsuario = async (id) => {
    const confirmar = window.confirm(
      `Tem certeza que deseja excluir o usuário #${id}?`
    );
    if (!confirmar) return;

    try {
      setExcluindoId(id);
      setError(null);
      setMensagem(null);

      await ExcluirUsuario(id);

      // Atualização otimista — remove da lista local
      setUsuarios((atuais) =>
        atuais.filter((usuario) => usuario.id !== id)
      );
      setMensagem("Usuário removido com sucesso");
    } catch (error) {
      console.error("Erro ao excluir usuário:", error);
      setError(error.message || "Erro ao excluir usuário");
    } finally {
      setExcluindoId(null);
    }
  };

  if (loading) {
    return (
      <div className="usuarios-page">
        <p className="loading">
          Carregando usuários...
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

        <h2>Usuários cadastrados</h2>

        <table className="usuarios-tabela">

          <thead>
            <tr>
              <th>ID</th>
              <th>Nome</th>
              <th>Email</th>
              <th>Tipo</th>
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

                <td>
                  <span
                    className={
                      usuario.tipo === "FUNCIONARIO"
                        ? "tipo funcionario"
                        : "tipo cliente"
                    }
                  >
                    {usuario.tipo}
                  </span>
                </td>

                <td className="acao-coluna">

                  <button
                    className="btn-excluir"
                    disabled={excluindoId === usuario.id}
                    onClick={() =>
                      excluirUsuario(usuario.id)
                    }
                  >
                    {excluindoId === usuario.id ? "Excluindo..." : "Excluir"}
                  </button>

                </td>

              </tr>
            ))}

          </tbody>

        </table>

      </div>

    </div>
  );
}

export default Usuarios;