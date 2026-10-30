import { useState } from "react";
import { FiEye, FiEyeOff } from "react-icons/fi";

export default function CampoSenha({ value, onChange, cadastro = false, disabled = false }) {
    const [visivel, setVisivel] = useState(false);
    return (
        <div className="auth-field">
            <label htmlFor="senha">Senha</label>
            <div className="auth-password">
                <input id="senha" name="senha" type={visivel ? "text" : "password"}
                    placeholder={cadastro ? "Crie sua senha" : "Digite sua senha"}
                    value={value} onChange={onChange} required disabled={disabled}
                    autoComplete={cadastro ? "new-password" : "current-password"} />
                <button type="button" className="auth-password-toggle" aria-label={visivel ? "Ocultar senha" : "Mostrar senha"}
                    aria-pressed={visivel} aria-controls="senha" disabled={disabled} onClick={() => setVisivel(!visivel)}>
                    {visivel ? <FiEyeOff aria-hidden="true" /> : <FiEye aria-hidden="true" />}
                </button>
            </div>
        </div>
    );
}
