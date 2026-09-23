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

import { useAuth } from "../../auth/AuthContext.jsx";

function Sidebar() {
    const navigate = useNavigate();
    const { user, logout } = useAuth();

    const sair = () => {
        if (!window.confirm("Tem certeza que deseja sair?")) return;
        logout();
        navigate("/");
    };

    const inicial = (user?.nome || "U").charAt(0).toUpperCase();

    return (
        <aside className="sidebar">

            <div>

                <div className="sidebar-logo">
                    <div className="logo-quadrado"></div>
                    <h2>FastFarma</h2>
                </div>

                <div className="sidebar-user">
                    <div className="user-avatar">{inicial}</div>
                    <strong>{user?.nome || "Usuário"}</strong>
                </div>

                <nav className="sidebar-menu">

                    <NavLink to="." end className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaChartLine /> Dashboard
                    </NavLink>

                    <NavLink to="pedidos" className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaClipboardList /> Pedidos
                    </NavLink>

                    <NavLink to="produtos" className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaBox /> Produtos
                    </NavLink>

                    <div className="menu-separador"></div>

                    <NavLink to="usuarios" className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaUsers /> Usuários
                    </NavLink>

                    <NavLink to="estoque" className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaBoxes /> Estoque
                    </NavLink>

                    <NavLink to="relatorio" className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaFileAlt /> Relatório
                    </NavLink>

                    <div className="menu-separador"></div>

                    <NavLink to="perfil" className={({ isActive }) =>
                        isActive ? "menu-link active" : "menu-link"}>
                        <FaUser /> Meu Perfil
                    </NavLink>

                </nav>

            </div>

            <button className="btn-sair" onClick={sair}>
                <FaSignOutAlt /> Sair
            </button>

        </aside>
    );
}

export default Sidebar;
