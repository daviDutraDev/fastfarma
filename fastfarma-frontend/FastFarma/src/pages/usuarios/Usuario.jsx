import { useState, useEffect } from "react";
import { BuscarUsuarios } from "../../services/api/Usuarios";
import "./Usuario.css";

function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [mensagem, setMensagem] = useState(null);

  useEffect(() => {
    const fetchUsuarios = async () => {
      try {
        setLoading(true);
        setError(null);
        setMensagem(null);

        const data = await BuscarUsuarios();

        setUsuarios(data.dados);

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
    };

    fetchUsuarios();
  }, []);

  const excluirUsuario = (id) => {
    const desejaExcluir = window.confirm(
      "Você realmente deseja excluir este usuário?"
    );
    if (!desejaExcluir) return;

    const novosUsuarios = usuarios.filter(
      (usuario) => usuario.id !== id
    );

    setUsuarios(novosUsuarios);

    setMensagem("Usuário removido da lista");
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
                    onClick={() =>
                      excluirUsuario(usuario.id)
                    }
                  >
                    Excluir
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