import { useLocation } from "react-router-dom";
import "./Header.css";

function Header() {
  const location = useLocation();

  const titulos = {
    "/painel": "Dashboard",
    "/painel/pedidos": "Pedidos",
    "/painel/produtos": "Produtos",
    "/painel/usuarios": "Usuários",
  };

  const titulo = titulos[location.pathname] || "FastFarma";

  return (
    <header className="header">
      <h1>{titulo}</h1>
    </header>
  );
}

export default Header;