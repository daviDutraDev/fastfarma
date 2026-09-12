import { NavLink, useNavigate } from "react-router-dom";
import './Sidebar.css';

import {
  FaChartLine,
  FaClipboardList,
  FaBox,
  FaUsers,
  FaSignOutAlt
} from "react-icons/fa";

import "./Sidebar.css";

function Sidebar() {
  const navigate = useNavigate();

  // Lê o usuário persistido no login. Falha de parse não derruba a UI.
  let usuario = null;
  try {
    const raw = localStorage.getItem("usuario");
    if (raw) usuario = JSON.parse(raw);
  } catch {
    usuario = null;
  }

  const sair = () => {
    const confirmarSaida = window.confirm(
      "Tem certeza que deseja sair?"
    );

    if (confirmarSaida) {
      localStorage.removeItem("usuario");
      navigate("/");
    }
  };

  return (
    <aside className="sidebar">

      <div>

        <div className="sidebar-logo">

          <div className="logo-quadrado"></div>

          <h2>FastFarma</h2>

        </div>


        <div className="sidebar-user">

          <div className="user-avatar">
            {usuario?.nome
              ? usuario.nome.charAt(0).toUpperCase()
              : "U"}
          </div>

          <strong>
            {usuario?.nome || "Usuário"}
          </strong>

          {usuario?.tipo && (
            <small className="sidebar-user-tipo">
              {usuario.tipo}
            </small>
          )}

        </div>


        <nav className="sidebar-menu">

          <NavLink
            to="."
            end
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaChartLine />
            Dashboard
          </NavLink>


          <NavLink
            to="pedidos"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaClipboardList />
            Pedidos
          </NavLink>


          <NavLink
            to="produtos"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaBox />
            Produtos
          </NavLink>


          <div className="menu-separador"></div>


          <NavLink
            to="usuarios"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaUsers />
            Usuários
          </NavLink>

        </nav>

      </div>


      <button
        className="btn-sair"
        onClick={sair}
      >
        <FaSignOutAlt />

        Sair
      </button>

    </aside>
  );
}

export default Sidebar;