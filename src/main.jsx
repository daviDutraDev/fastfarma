import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'

import Login from './pages/login/Login.jsx'
import CadastrarUser from './pages/cadastrarUsuario/CadastrarUser.jsx'

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
  }

])

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
)
