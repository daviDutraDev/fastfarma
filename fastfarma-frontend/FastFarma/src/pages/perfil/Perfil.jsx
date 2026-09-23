import { useEffect, useState } from "react";
import { BuscarMeuPerfil, AtualizarMeuPerfil, TrocarMinhaSenha } from "../../services/api/Perfil";
import { useAuth } from "../../auth/AuthContext.jsx";
import "./Perfil.css";

const formatarTelefone = (valor) => {
    const d = (valor || "").replace(/\D/g, "").slice(0, 11);
    if (d.length <= 2) return d;
    if (d.length <= 6) return d.replace(/(\d{2})(\d+)/, "($1) $2");
    if (d.length <= 10) return d.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3");
    return d.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3");
};

function Perfil() {
    const { user } = useAuth();

    const [nome, setNome] = useState("");
    const [telefone, setTelefone] = useState("");
    const [senhaAtual, setSenhaAtual] = useState("");
    const [novaSenha, setNovaSenha] = useState("");
    const [confirmaSenha, setConfirmaSenha] = useState("");

    const [loadingPerfil, setLoadingPerfil] = useState(false);
    const [loadingSenha, setLoadingSenha] = useState(false);
    const [mensagem, setMensagem] = useState(null);
    const [error, setError] = useState(null);

    // Carrega dados iniciais
    useEffect(() => {
        const carregar = async () => {
            setLoadingPerfil(true);
            setError(null);
            try {
                const resp = await BuscarMeuPerfil();
                const dados = resp?.dados;
                if (dados) {
                    setNome(dados.nome || "");
                    setTelefone(formatarTelefone(dados.telefone || ""));
                }
            } catch (e) {
                setError(e.message || "Erro ao carregar perfil");
            } finally {
                setLoadingPerfil(false);
            }
        };
        carregar();
    }, []);

    const salvarPerfil = async (e) => {
        e.preventDefault();
        setMensagem(null);
        setError(null);
        setLoadingPerfil(true);
        try {
            const resp = await AtualizarMeuPerfil({
                nome,
                telefone: telefone.replace(/\D/g, "") || null,
            });
            setMensagem("Perfil atualizado com sucesso.");
            // Recarrega os dados para refletir telefone novo (essencial
            // para a notificacao WhatsApp usar o numero cadastrado).
            if (resp?.dados) {
                // recarrega silenciosamente em background
                BuscarMeuPerfil()
                    .then((r) => {
                        const d = r?.dados;
                        if (d) setTelefone(formatarTelefone(d.telefone || ""));
                    })
                    .catch(() => {});
            }
        } catch (e) {
            setError(e.message || "Erro ao atualizar perfil");
        } finally {
            setLoadingPerfil(false);
        }
    };

    const salvarSenha = async (e) => {
        e.preventDefault();
        setMensagem(null);
        setError(null);

        if (novaSenha.length < 4) {
            setError("A nova senha deve ter pelo menos 4 caracteres.");
            return;
        }
        if (novaSenha !== confirmaSenha) {
            setError("A confirmação não confere com a nova senha.");
            return;
        }
        if (!senhaAtual) {
            setError("Informe a senha atual.");
            return;
        }

        setLoadingSenha(true);
        try {
            await TrocarMinhaSenha(senhaAtual, novaSenha);
            setMensagem("Senha alterada com sucesso.");
            setSenhaAtual("");
            setNovaSenha("");
            setConfirmaSenha("");
        } catch (e) {
            setError(e.message || "Erro ao trocar senha");
        } finally {
            setLoadingSenha(false);
        }
    };

    if (loadingPerfil && !nome) {
        return <div className="perfil-page"><p>Carregando...</p></div>;
    }

    return (
        <div className="perfil-page">

            <header className="perfil-header">
                <h1>Meu Perfil</h1>
                <p>Atualize seus dados pessoais e sua senha.</p>
            </header>

            {mensagem && <div className="perfil-msg sucesso">{mensagem}</div>}
            {error && <div className="perfil-msg erro">{error}</div>}

            <div className="perfil-grid">

                {/* Card: dados pessoais */}
                <section className="perfil-card">
                    <h2>Dados pessoais</h2>

                    <form className="perfil-form" onSubmit={salvarPerfil}>

                        <div className="form-group">
                            <label htmlFor="perfil-email">Email</label>
                            <input id="perfil-email" type="email"
                                   value={user?.email || ""} disabled />
                            <small>O email não pode ser alterado.</small>
                        </div>

                        <div className="form-group">
                            <label htmlFor="perfil-tipo">Tipo</label>
                            <input id="perfil-tipo" type="text"
                                   value={user?.tipo || ""} disabled />
                        </div>

                        <div className="form-group">
                            <label htmlFor="perfil-nome">Nome</label>
                            <input id="perfil-nome" type="text"
                                   value={nome}
                                   onChange={(e) => setNome(e.target.value)}
                                   required minLength={2} maxLength={100} />
                        </div>

                        <div className="form-group">
                            <label htmlFor="perfil-telefone">
                                Telefone (WhatsApp) <span className="campo-opcional">opcional</span>
                            </label>
                            <input id="perfil-telefone" type="tel"
                                   inputMode="numeric"
                                   placeholder="(47) 99999-9999"
                                   value={telefone}
                                   onChange={(e) => setTelefone(formatarTelefone(e.target.value))} />
                        </div>

                        <button type="submit" className="btn-perfil" disabled={loadingPerfil}>
                            {loadingPerfil ? "Salvando..." : "Salvar dados"}
                        </button>
                    </form>
                </section>

                {/* Card: trocar senha */}
                <section className="perfil-card">
                    <h2>Trocar senha</h2>

                    <form className="perfil-form" onSubmit={salvarSenha}>

                        <div className="form-group">
                            <label htmlFor="perfil-senha-atual">Senha atual</label>
                            <input id="perfil-senha-atual" type="password"
                                   value={senhaAtual}
                                   onChange={(e) => setSenhaAtual(e.target.value)}
                                   required autoComplete="current-password" />
                        </div>

                        <div className="form-group">
                            <label htmlFor="perfil-nova-senha">Nova senha</label>
                            <input id="perfil-nova-senha" type="password"
                                   value={novaSenha}
                                   onChange={(e) => setNovaSenha(e.target.value)}
                                   required minLength={4}
                                   autoComplete="new-password" />
                            <small>Mínimo 4 caracteres.</small>
                        </div>

                        <div className="form-group">
                            <label htmlFor="perfil-confirma-senha">Confirmar nova senha</label>
                            <input id="perfil-confirma-senha" type="password"
                                   value={confirmaSenha}
                                   onChange={(e) => setConfirmaSenha(e.target.value)}
                                   required minLength={4}
                                   autoComplete="new-password" />
                        </div>

                        <button type="submit" className="btn-perfil" disabled={loadingSenha}>
                            {loadingSenha ? "Trocando..." : "Trocar senha"}
                        </button>
                    </form>
                </section>

            </div>

        </div>
    );
}

export default Perfil;
