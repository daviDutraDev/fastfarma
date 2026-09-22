import { useState } from 'react'
import './Login.css'
import { useNavigate } from 'react-router-dom'
import { FazerLogin } from '../../services/api/AuthApi'
import { useAuth } from '../../auth/AuthContext.jsx'

const Login = () => {
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [mensagem, setMensagem] = useState(null);

    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();

        setLoading(true);
        setError("");
        setMensagem("");

        try {
            const result = await login(email, senha);
            setMensagem("Login realizado com sucesso!");

            setTimeout(() => {
                setEmail("");
                setSenha("");
                setLoading(false);

                // Roteia por role (admin -> /painel, cliente -> /usuario)
                if (result?.user?.tipo === "FUNCIONARIO") {
                    navigate("/painel");
                } else {
                    navigate("/usuario");
                }
            }, 800);

        } catch (err) {
            setError(err.message || "Erro ao realizar login");
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
                            autoComplete="email"
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
                            autoComplete="current-password"
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
