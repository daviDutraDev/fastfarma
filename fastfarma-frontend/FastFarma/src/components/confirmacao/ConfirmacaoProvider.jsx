import { useEffect, useId, useRef, useState } from "react";
import { FiAlertTriangle, FiLogOut } from "react-icons/fi";
import { ConfirmacaoContext } from "./ConfirmacaoContext.js";
import "./Confirmacao.css";

export function ConfirmacaoProvider({ children }) {
  const [opcoes, setOpcoes] = useState(null);
  const resolver = useRef(null);
  const dialog = useRef(null);
  const tituloId = useId();
  const descricaoId = useId();

  const confirmar = (configuracao) => {
    // Uma nova solicitação cancela a anterior, caso ainda esteja aberta.
    resolver.current?.(false);
    return new Promise((resolve) => {
      resolver.current = resolve;
      setOpcoes(configuracao);
    });
  };

  const responder = (resposta) => {
    resolver.current?.(resposta);
    resolver.current = null;
    setOpcoes(null);
  };

  useEffect(() => {
    if (!opcoes) return undefined;
    const elemento = dialog.current;
    elemento.showModal();
    const overflowAnterior = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      elemento.close();
      document.body.style.overflow = overflowAnterior;
    };
  }, [opcoes]);

  useEffect(() => () => resolver.current?.(false), []);

  return (
    <ConfirmacaoContext.Provider value={confirmar}>
      {children}
      {opcoes && (
        <dialog
          ref={dialog}
          className="confirmacao-dialog"
          aria-labelledby={tituloId}
          aria-describedby={descricaoId}
          onCancel={(event) => {
            event.preventDefault();
            responder(false);
          }}
          onKeyDown={(event) => event.stopPropagation()}
        >
          <div className={`confirmacao-icone ${opcoes.perigo ? "confirmacao-perigo" : ""}`}>
            {opcoes.perigo ? <FiAlertTriangle aria-hidden="true" /> : <FiLogOut aria-hidden="true" />}
          </div>
          <h2 id={tituloId}>{opcoes.titulo}</h2>
          <p id={descricaoId}>{opcoes.descricao}</p>
          <div className="confirmacao-botoes">
            <button type="button" className="confirmacao-cancelar" autoFocus onClick={() => responder(false)}>
              {opcoes.cancelarTexto || "Voltar"}
            </button>
            <button
              type="button"
              className={`confirmacao-confirmar ${opcoes.perigo ? "confirmacao-perigo" : ""}`}
              onClick={() => responder(true)}
            >
              {opcoes.confirmarTexto || "Confirmar"}
            </button>
          </div>
        </dialog>
      )}
    </ConfirmacaoContext.Provider>
  );
}
