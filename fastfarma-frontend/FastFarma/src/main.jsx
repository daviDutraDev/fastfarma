import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'

import Login from './pages/login/Login.jsx'
import CadastrarUser from './pages/cadastrarUsuario/CadastrarUser.jsx'
import Dashboard from './pages/dashboard/DashBoard.jsx'
import MainLayout from './layouts/menu/MainLayout.jsx'
import Pedido from './pages/pedidos/Pedidos.jsx'
import Produtos from './pages/produtos/Produtos.jsx'
import Usuarios from './pages/usuarios/Usuario.jsx'
import UserLayout from './layouts/usuario/UserLayout.jsx'
import DashBoardUser from './pages/usuariosPage/dashboard/DashBoardUser.jsx'
import Estoque from './pages/estoque/Estoque.jsx'
import PedidoUser from './pages/usuariosPage/pedido/PedidoUser.jsx'
import FazerPedido from './pages/usuariosPage/fazerPedido/FazerPedido.jsx'


import {
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";

const router = createBrowserRouter([
  {
    path: '/',
    element: <Login />,
  },
  {
    path: '/cadastrar',
    element: <CadastrarUser />
  },
  {
    path: '/painel', 
    element: <MainLayout />,
    children: [
      {
        index: true,
        element: <Dashboard />
      },
      {
        path: 'pedidos',
        element: <Pedido />
      },
      {
        path: 'produtos',
        element: <Produtos />
      },
      {
        path: 'usuarios',
        element: <Usuarios />
      },
      {
        path: 'estoque',
        element: <Estoque />
      }
    ]
  },
  {
    path: '/usuario',
    element: <UserLayout />,
    children: [
      {
        index: true,
        element: <DashBoardUser />
      },
      {
        path: 'pedidos',
        element: <PedidoUser />
      },
      {
        path: 'fazer-pedido',
        element: <FazerPedido />
      }
    ]
  }

])

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
