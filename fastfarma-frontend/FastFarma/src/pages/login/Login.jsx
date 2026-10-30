import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext.jsx";
import AuthLayout from "../../components/auth/AuthLayout.jsx";
import CampoSenha from "../../components/auth/CampoSenha.jsx";

const Login = () => {
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [mensagem, setMensagem] = useState(null);
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (loading) return;
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
                navigate(result?.user?.tipo === "FUNCIONARIO" ? "/painel" : "/usuario");
            }, 800);
        } catch (err) {
            setError(err.message || "Erro ao realizar login");
            setLoading(false);
        }
    };

    return (
        <AuthLayout busy={loading}>
            <form className="auth-form" onSubmit={handleSubmit} aria-busy={loading}>
                <div className="auth-field">
                    <label htmlFor="email">E-mail</label>
                    <input id="email" name="email" type="email" placeholder="seuemail@exemplo.com"
                        value={email} onChange={(e) => setEmail(e.target.value)} required
                        autoComplete="email" disabled={loading} />
                </div>
                <CampoSenha value={senha} onChange={(e) => setSenha(e.target.value)} disabled={loading} />
                <button type="submit" className="auth-submit" disabled={loading}>
                    {loading ? "Carregando..." : "Entrar"}
                </button>
            </form>
            {loading && <p className="loading-message" role="status">Realizando login...</p>}
            {error && <p className="error-message" role="alert">{error}</p>}
            {mensagem && <p className="success-message" role="status">{mensagem}</p>}
        </AuthLayout>
    );
};

export default Login;
