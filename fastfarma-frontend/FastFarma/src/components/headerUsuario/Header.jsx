import { useLocation } from "react-router-dom";
import "./Header.css";

function Header() {
  const location = useLocation();

  const titulos = {
    "/usuario": "Dashboard do Usuário",
    "/usuario/pedidos": "Meus Pedidos",
    "/usuario/fazer-pedido": "Fazer Pedido",
    "/usuario/perfil": "Meu Perfil",
  };

  const titulo = titulos[location.pathname] || "FastFarma";

  return (
    <header className="header">
      <h2>{titulo}</h2>
    </header>
  );
}

export default Header;