import { useState } from 'react'
import './Login.css'
import { useNavigate } from 'react-router-dom'

import { FazerLogin } from '../../services/api/AuthApi'


const Login = () => {
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [mensagem, setMensagem] = useState(null);

    const navigate = useNavigate()

    const handleSubmit = async (e) => {
    e.preventDefault();

    setLoading(true);
    setError("");
    setMensagem("");

    try {
        const data = await FazerLogin(email, senha);
        console.log(data)

        setMensagem(data.mensagem || "Login realizado com sucesso");

        setTimeout(() => {
            setEmail("");
            setSenha("");
            setLoading(false);
            navigate("/painel");
        }, 2000);

    } catch (error) {
        setError(error.message || "Erro ao realizar login");
        setLoading(false);
    }
};

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-header">
                    <h2 className="login-title">FastFarma</h2>
                    <p className="login-subtitle">Sistema de atendimento</p>
                </div>

                <form className="login-form" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            id="email"
                            type="email"
                            placeholder="Digite seu email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="senha">Senha</label>
                        <input
                            id="senha"
                            type="password"
                            placeholder="Digite sua senha"
                            value={senha}
                            onChange={(e) => setSenha(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="btn-submit" disabled={loading}>
                        {loading ? 'Carregando...' : 'Entrar'}
                    </button>
                </form>

                {loading && <p className="loading-message">Realizando login...</p>}
                {error && <p className="error-message">{error}</p>}
                {mensagem && <p className="success-message">{mensagem}</p>}

                <p className="login-footer">
                    Ainda não tem uma conta?
                    <button
                        type="button"
                        className="btn-link"
                        onClick={() => navigate('/cadastrar')}
                    >
                        Criar conta
                    </button>
                </p>
            </div>
        </div>
    )
}

export default Login