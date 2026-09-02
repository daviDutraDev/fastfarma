import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'

import Login from './pages/login/Login.jsx'
import CadastrarUser from './pages/cadastrarUsuario/CadastrarUser.jsx'
import Dashboard from './pages/dashboard/DashBoard.jsx'
import MainLayout from './layouts/menu/MainLayout.jsx'
import Pedido from './pages/pedidos/Pedidos.jsx'


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
      }
    ]
  }

])

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
