import { useState } from 'react'
import { CadastrarUsuario } from '../../services/api/CadastroUserApi'
import './CadastrarUser.css'
import { useNavigate } from 'react-router-dom'

const CadastrarUser = () => {
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [nome, setNome] = useState('')

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
        const data = await CadastrarUsuario(nome, email, senha);

        setMensagem(data.mensagem || "Usuário cadastrado com sucesso");
        console.log(data)

        setTimeout(() => {
            setEmail("");
            setSenha("");
            setLoading(false);
            navigate("/dashboard");
        }, 2000);

    } catch (error) {
        setError(error.message || "Erro ao cadastrar usuário");
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
                        <label htmlFor="nome">Nome</label>
                        <input
                            id="nome"
                            type="text"
                            placeholder="Digite seu nome"
                            value={nome}
                            onChange={(e) => setNome(e.target.value)}
                            required
                        />
                    </div>

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
                        {loading ? 'Cadastrando...' : 'Cadastrar'}
                    </button>
                </form>

                {loading && <p className="loading-message">Cadastrando...</p>}
                {error && <p className="error-message">{error}</p>}
                {mensagem && <p className="success-message">{mensagem}</p>}

                <p className="login-footer">
                    Ja possui uma conta?
                    <button
                        type="button"
                        className="btn-link"
                        disabled={loading}
                        onClick={() => navigate('/')}
                    >
                        faça login
                    </button>
                </p>
            </div>
        </div>
  )
}

export default CadastrarUser