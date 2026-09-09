import { useState } from "react";
import "./Usuario.css";

function Usuarios() {
  const [usuarios, setUsuarios] = useState([
    {
      id: 1,
      nome: "admin",
      email: "admin@gmail.com",
      tipo: "FUNCIONARIO",
    },
    {
      id: 2,
      nome: "davi",
      email: "davi@gmail.com",
      tipo: "CLIENTE",
    },
    {
      id: 3,
      nome: "caua",
      email: "caua@gmail.com",
      tipo: "FUNCIONARIO",
    },
    {
      id: 4,
      nome: "mariana",
      email: "mariana@gmail.com",
      tipo: "CLIENTE",
    },
  ]);

  const excluirUsuario = (id) => {
    const novosUsuarios = usuarios.filter(
      (usuario) => usuario.id !== id
    );

    setUsuarios(novosUsuarios);
  };

  return (
    <div className="usuarios-page">

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

                <td>{usuario.id}</td>

                <td>{usuario.nome}</td>

                <td>{usuario.email}</td>

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