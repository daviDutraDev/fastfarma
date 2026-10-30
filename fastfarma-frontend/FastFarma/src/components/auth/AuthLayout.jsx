import { Link } from "react-router-dom";
import { FiUser } from "react-icons/fi";
import logo from "../../assets/fastfarma-logo.png";
import "./AuthLayout.css";

export default function AuthLayout({ cadastro = false, busy = false, children }) {
    const destino = cadastro ? "/" : "/cadastrar";
    const textoLink = cadastro ? "Entrar" : "Criar conta";
    const bloquearDuranteEnvio = (event) => { if (busy) event.preventDefault(); };

    return (
        <div className={`auth-page${cadastro ? " auth-page-cadastro" : ""}`}>
            <div className="auth-motif auth-motif-left" aria-hidden="true"><img src={logo} alt="" /></div>
            <div className="auth-motif auth-motif-right" aria-hidden="true"><img src={logo} alt="" /></div>
            <header className="auth-topbar">
                <Link to="/" className="auth-brand" aria-label="FastFarma — início" onClick={bloquearDuranteEnvio} aria-disabled={busy}>
                    <img src={logo} alt="FastFarma" width="220" height="74" />
                </Link>
                <Link className="auth-top-link" to={destino} onClick={bloquearDuranteEnvio} aria-disabled={busy}>{textoLink}</Link>
            </header>
            <main className="auth-main">
                <section className="auth-card" aria-labelledby="auth-title">
                    <div className="auth-card-header">
                        <span className="auth-avatar" aria-hidden="true"><FiUser /></span>
                        <h1 id="auth-title">{cadastro ? "Seu primeiro passo começa aqui." : "Bom ter você por aqui."}</h1>
                        <p>{cadastro ? "Crie sua conta para fazer e acompanhar pedidos." : "Entre na sua conta FastFarma."}</p>
                    </div>
                    {children}
                    <p className="auth-card-footer">
                        {cadastro ? "Já tem uma conta?" : "Ainda não tem uma conta?"}{" "}
                        <Link to={destino} onClick={bloquearDuranteEnvio} aria-disabled={busy}>{textoLink}</Link>
                    </p>
                </section>
                <footer className="auth-footer">FastFarma · Cuidado em cada pedido</footer>
            </main>
        </div>
    );
}
