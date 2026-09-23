import { NavLink, useNavigate } from "react-router-dom";
import {
    FaBoxOpen,
    FaClipboardList,
    FaShoppingCart,
    FaUser,
    FaSignOutAlt,
} from "react-icons/fa";

import { useAuth } from "../../auth/AuthContext.jsx";
import "./Sidebar.css";

function Sidebar() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    const sair = () => {
        if (!window.confirm("Você realmente deseja sair do sistema?")) return;
        logout();
        navigate("/");
    };

    const inicial = (user?.nome || "U").substring(0, 2).toUpperCase();

    return (
        <aside className="sidebar">
            <div className="sidebar-conteudo">

                <div className="sidebar-logo">
                    <span className="logo-quadrado"></span>
                    <h1>FastFarma</h1>
                </div>

                <div className="sidebar-usuario">
                    <div className="usuario-avatar">{inicial}</div>
                    <span>{user?.nome || "Usuário"}</span>
                </div>

                <nav className="sidebar-menu">

                    <NavLink to="." end
                             className={({ isActive }) =>
                                 isActive ? "menu-link ativo" : "menu-link"}>
                        <FaBoxOpen />
                        <span>DashBoard</span>
                    </NavLink>

                    <NavLink to="fazer-pedido"
                             className={({ isActive }) =>
                                 isActive ? "menu-link ativo" : "menu-link"}>
                        <FaShoppingCart />
                        <span>Fazer Pedido</span>
                    </NavLink>

                    <NavLink to="pedidos"
                             className={({ isActive }) =>
                                 isActive ? "menu-link ativo" : "menu-link"}>
                        <FaClipboardList />
                        <span>Meus Pedidos</span>
                    </NavLink>

                    <div className="menu-separador"></div>

                    <NavLink to="perfil"
                             className={({ isActive }) =>
                                 isActive ? "menu-link ativo" : "menu-link"}>
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
