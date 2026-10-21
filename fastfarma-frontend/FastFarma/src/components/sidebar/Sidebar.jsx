import { NavLink, useNavigate, useLocation } from "react-router-dom";
import { useState } from "react";
import { useConfirmacao } from "../confirmacao/ConfirmacaoContext.js";
import './Sidebar.css';

import {
    FaChartLine,
    FaClipboardList,
    FaBox,
    FaUsers,
    FaBoxes,
    FaFileAlt,
    FaUser,
    FaSignOutAlt,
    FaUserTie,
    FaUserFriends,
    FaChevronDown
} from "react-icons/fa";

import { useAuth } from "../../auth/AuthContext.jsx";

function Sidebar() {
    const navigate = useNavigate();
    const location = useLocation();
    const { user, logout } = useAuth();
    const confirmar = useConfirmacao();

    // A pasta "Usuários" abre sozinha se a pessoa já está numa das
    // sub-telas (ex.: deu refresh em /painel/usuarios/clientes) e
    // pode ser aberta/fechada manualmente clicando nela.
    const emUsuarios = location.pathname.startsWith("/painel/usuarios");
    const [usuariosAberto, setUsuariosAberto] = useState(emUsuarios);

    const sair = async () => {
        if (!await confirmar({
            titulo: "Sair da sua conta?",
            descricao: "Você precisará entrar novamente para acessar o sistema.",
            cancelarTexto: "Continuar conectado",
            confirmarTexto: "Sair",
        })) return;
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

                    {/* "Pasta" Usuários — clicar abre/fecha, sem navegar
                        sozinha, já que agora tem duas sub-telas dentro. */}
                    <button
                        type="button"
                        className={
                            "menu-link menu-pasta" +
                            (emUsuarios ? " active" : "")
                        }
                        onClick={() => setUsuariosAberto((aberto) => !aberto)}
                        aria-expanded={usuariosAberto}
                    >
                        <FaUsers /> Usuários
                        <FaChevronDown
                            className={
                                "menu-pasta-seta" +
                                (usuariosAberto ? " aberta" : "")
                            }
                        />
                    </button>

                    {usuariosAberto && (
                        <div className="submenu">
                            <NavLink to="usuarios/clientes" className={({ isActive }) =>
                                isActive ? "menu-link submenu-link active" : "menu-link submenu-link"}>
                                <FaUserFriends /> Clientes
                            </NavLink>

                            <NavLink to="usuarios/funcionarios" className={({ isActive }) =>
                                isActive ? "menu-link submenu-link active" : "menu-link submenu-link"}>
                                <FaUserTie /> Funcionários
                            </NavLink>
                        </div>
                    )}

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
