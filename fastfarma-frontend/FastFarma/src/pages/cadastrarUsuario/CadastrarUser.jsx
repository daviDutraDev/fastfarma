import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { CadastrarUsuario } from "../../services/api/CadastroUserApi";
import AuthLayout from "../../components/auth/AuthLayout.jsx";
import CampoSenha from "../../components/auth/CampoSenha.jsx";

const CadastrarUser = () => {
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [nome, setNome] = useState("");
    const [telefone, setTelefone] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [mensagem, setMensagem] = useState(null);
    const navigate = useNavigate();

    const formatarTelefone = (valor) => {
        const numeros = valor.replace(/\D/g, "").slice(0, 11);
        if (numeros.length <= 2) return numeros;
        if (numeros.length <= 6) return numeros.replace(/(\d{2})(\d+)/, "($1) $2");
        if (numeros.length <= 10) return numeros.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
        return numeros.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (loading) return;
        setLoading(true);
        setError("");
        setMensagem("");
        try {
            const data = await CadastrarUsuario(nome, email, senha, telefone);
            setMensagem(data.mensagem || "Usuário cadastrado com sucesso");
            setTimeout(() => {
                setEmail("");
                setSenha("");
                setTelefone("");
                setLoading(false);
                navigate("/");
            }, 2000);
        } catch (error) {
            setError(error.message || "Erro ao cadastrar usuário");
            setLoading(false);
        }
    };

    return (
        <AuthLayout cadastro busy={loading}>
            <form className="auth-form" onSubmit={handleSubmit} aria-busy={loading}>
                <div className="auth-field">
                    <label htmlFor="nome">Nome completo</label>
                    <input id="nome" name="nome" type="text" placeholder="Seu nome" value={nome}
                        onChange={(e) => setNome(e.target.value)} required autoComplete="name" disabled={loading} />
                </div>
                <div className="auth-field">
                    <label htmlFor="email">E-mail</label>
                    <input id="email" name="email" type="email" placeholder="seuemail@exemplo.com" value={email}
                        onChange={(e) => setEmail(e.target.value)} required autoComplete="email" disabled={loading} />
                </div>
                <div className="auth-field">
                    <label htmlFor="telefone">WhatsApp <span>(opcional)</span></label>
                    <input id="telefone" name="telefone" type="tel" inputMode="numeric" placeholder="(00) 00000-0000"
                        value={telefone} onChange={(e) => setTelefone(formatarTelefone(e.target.value))}
                        autoComplete="tel-national" disabled={loading} />
                </div>
                <CampoSenha cadastro value={senha} onChange={(e) => setSenha(e.target.value)} disabled={loading} />
                <button type="submit" className="auth-submit" disabled={loading}>
                    {loading ? "Cadastrando..." : "Criar conta"}
                </button>
            </form>
            {loading && <p className="loading-message" role="status">Cadastrando...</p>}
            {error && <p className="error-message" role="alert">{error}</p>}
            {mensagem && <p className="success-message" role="status">{mensagem}</p>}
        </AuthLayout>
    );
};

export default CadastrarUser;
