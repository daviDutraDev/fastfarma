import { Link } from "react-router-dom";
import {
    FaBoxOpen,
    FaShoppingCart,
    FaClipboardList,
    FaUser,
} from "react-icons/fa";

import { useAuth } from "../../../auth/AuthContext.jsx";
import "./DashboardUser.css";

function DashboardUser() {
    const { user } = useAuth();

    const horaAtual = new Date().getHours();
    const saudacao =
        horaAtual < 12 ? "Bom dia" :
        horaAtual < 18 ? "Boa tarde" :
        "Boa noite";

    const primeiroNome = (user?.nome || "").split(" ")[0] || "usuário";

    return (
        <main className="dashboard-usuario">

            <section className="boas-vindas">
                <div>
                    <span className="boas-vindas-detalhe">Bem-vindo à FastFarma</span>
                    <h1>{saudacao}, {primeiroNome}!</h1>
                    <p>
                        Encontre seus produtos, acompanhe seus pedidos e
                        gerencie sua conta em um só lugar.
                    </p>

                    <Link to="/usuario/fazer-pedido" className="btn-pedido">
                        <FaShoppingCart />
                        Fazer novo pedido
                    </Link>
                </div>

                <div className="boas-vindas-icone">
                    <FaUser />
                </div>
            </section>

            <section className="atalhos">
                <h2>O que você deseja fazer?</h2>

                <div className="atalhos-container">

                    <Link to="/usuario/fazer-pedido" className="atalho-card">
                        <div className="atalho-icone">
                            <FaBoxOpen />
                        </div>
                        <div>
                            <h3>Ver produtos</h3>
                            <p>Consulte os produtos disponíveis.</p>
                        </div>
                    </Link>

                    <Link to="/usuario/fazer-pedido" className="atalho-card">
                        <div className="atalho-icone">
                            <FaShoppingCart />
                        </div>
                        <div>
                            <h3>Fazer pedido</h3>
                            <p>Escolha produtos e monte seu pedido.</p>
                        </div>
                    </Link>

                    <Link to="/usuario/pedidos" className="atalho-card">
                        <div className="atalho-icone">
                            <FaClipboardList />
                        </div>
                        <div>
                            <h3>Meus pedidos</h3>
                            <p>Acompanhe o andamento dos seus pedidos.</p>
                        </div>
                    </Link>

                </div>
            </section>

        </main>
    );
}

export default DashboardUser;
