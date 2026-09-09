import { useLocation } from "react-router-dom";
import "./Header.css";

function Header() {
  const location = useLocation();

  const titulos = {
    "/painel": "Dashboard",
    "/painel/pedidos": "Pedidos",
    "/painel/produtos": "Produtos",
    "/painel/usuarios": "Usuários",
    "/painel/estoque": "Estoque",
    "/painel/relatorio": "Relatório",
    "/painel/perfil": "Meu Perfil",
  };

  const titulo = titulos[location.pathname] || "FastFarma";

  return (
    <header className="header">
      <h1>{titulo}</h1>

      {location.pathname === "/painel/produtos" && (
        <button className="btn-pdf">
          Gerar Relatório PDF
        </button>
      )}
    </header>
  );
}

export default Header;