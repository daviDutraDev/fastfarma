import { NavLink, useNavigate } from "react-router-dom";
import './Sidebar.css';

import {
  FaChartLine,
  FaClipboardList,
  FaBox,
  FaUsers,
  FaBoxes,
  FaFileAlt,
  FaUser,
  FaSignOutAlt
} from "react-icons/fa";

import "./Sidebar.css";

function Sidebar() {
  const navigate = useNavigate();

  const usuario = JSON.parse(localStorage.getItem("usuario"));

  const sair = () => {
    localStorage.removeItem("usuario");
    navigate("/");
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
            to="/app/pedidos"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaClipboardList />
            Pedidos
          </NavLink>


          <NavLink
            to="/app/produtos"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaBox />
            Produtos
          </NavLink>


          <div className="menu-separador"></div>


          <NavLink
            to="/app/usuarios"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaUsers />
            Usuários
          </NavLink>


          <NavLink
            to="/app/estoque"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaBoxes />
            Estoque
          </NavLink>


          <NavLink
            to="/app/relatorio"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaFileAlt />
            Relatório
          </NavLink>


          <div className="menu-separador"></div>


          <NavLink
            to="/app/perfil"
            className={({ isActive }) =>
              isActive ? "menu-link active" : "menu-link"
            }
          >
            <FaUser />
            Meu Perfil
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