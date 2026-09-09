import { Outlet } from "react-router-dom";

import Sidebar from "../../components/sidebar/Sidebar.jsx";
import Header from "../../components/header/Header.jsx";

import "./MainLayout.css";

function MainLayout() {

  return (
    <div className="main-layout">

      <Sidebar />

      <Header />

      <main className="main-content">

        <Outlet />

      </main>

    </div>
  );
}

export default MainLayout;