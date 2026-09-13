import { NavLink, useNavigate } from "react-router-dom";
import {
  FaBoxOpen,
  FaClipboardList,
  FaShoppingCart,
  FaUser,
  FaSignOutAlt,
} from "react-icons/fa";

import "./Sidebar.css";

function Sidebar() {
  const navigate = useNavigate();

  const usuario = JSON.parse(localStorage.getItem("usuario"));

  const sair = () => {
    const desejaSair = window.confirm(
      "Você realmente deseja sair do sistema?"
    );

    if (!desejaSair) return;

    localStorage.removeItem("usuario");

    navigate("/");
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-conteudo">
        <div className="sidebar-logo">
          <span className="logo-quadrado"></span>
          <h1>FastFarma</h1>
        </div>

        <div className="sidebar-usuario">
          <div className="usuario-avatar">
            {usuario?.nome
              ? usuario.nome.substring(0, 2).toUpperCase()
              : "DA"}
          </div>

          <span>{usuario?.nome || "Davi"}</span>
        </div>

        <nav className="sidebar-menu">
           <NavLink
            to="."
            className={({ isActive }) =>
              isActive ? "menu-link ativo" : "menu-link"
            }
          >
            <FaBoxOpen />
            <span>DashBoard</span>
          </NavLink>


          <NavLink
            to="/usuario/produtos"
            className={({ isActive }) =>
              isActive ? "menu-link ativo" : "menu-link"
            }
          >
            <FaBoxOpen />
            <span>Produtos</span>
          </NavLink>

          <NavLink
            to="/app/pedidos"
            className={({ isActive }) =>
              isActive ? "menu-link ativo" : "menu-link"
            }
          >
            <FaClipboardList />
            <span>Meus Pedidos</span>
          </NavLink>

          <NavLink
            to="/app/fazer-pedido"
            className={({ isActive }) =>
              isActive ? "menu-link ativo" : "menu-link"
            }
          >
            <FaShoppingCart />
            <span>Fazer Pedido</span>
          </NavLink>

          <div className="menu-separador"></div>

          <NavLink
            to="/app/perfil"
            className={({ isActive }) =>
              isActive ? "menu-link ativo" : "menu-link"
            }
          >
            <FaUser />
            <span>Meu Perfil</span>
          </NavLink>
        </nav>
      </div>

      <button className="sidebar-sair" onClick={sair}>
        <FaSignOutAlt />
        <span>Sair</span>
      </button>
    </aside>
  );
}

export default Sidebar;