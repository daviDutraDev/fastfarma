import { Outlet } from "react-router-dom";

import Sidebar from "../../components/sidebarUsuario/Sidebar";
import Header from "../../components/headerUsuario/Header";

import "./UserLayout.css";

function UserLayout() {
  return (
    <div className="master-layout">
      <Sidebar />

      <div className="master-principal">
        <Header />

        <main className="master-conteudo">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default UserLayout;