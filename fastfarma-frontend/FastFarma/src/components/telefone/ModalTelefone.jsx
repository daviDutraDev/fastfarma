import { useEffect, useState } from "react";
import "./ModalTelefone.css";

function ModalTelefone({
  aberto,
  onClose,
  onConfirmar,
  total,
}) {
  const [telefone, setTelefone] = useState("");
  const [erro, setErro] = useState("");

  useEffect(() => {
    if (!aberto) {
      setTelefone("");
      setErro("");
    }
  }, [aberto]);

  if (!aberto) {
    return null;
  }

  const formatarTelefone = (valor) => {
    const numeros = valor.replace(/\D/g, "").slice(0, 11);

    if (numeros.length <= 2) {
      return numeros;
    }

    if (numeros.length <= 6) {
      return numeros.replace(
        /(\d{2})(\d+)/,
        "($1) $2"
      );
    }

    if (numeros.length <= 10) {
      return numeros.replace(
        /(\d{2})(\d{4})(\d{0,4})/,
        "($1) $2-$3"
      );
    }

    return numeros.replace(
      /(\d{2})(\d{5})(\d{0,4})/,
      "($1) $2-$3"
    );
  };

  const alterarTelefone = (event) => {
    const telefoneFormatado = formatarTelefone(
      event.target.value
    );

    setTelefone(telefoneFormatado);
    setErro("");
  };

  const confirmarTelefone = () => {
    const telefoneNumeros = telefone.replace(/\D/g, "");

    if (
      telefoneNumeros.length !== 10 &&
      telefoneNumeros.length !== 11
    ) {
      setErro("Informe um telefone válido com DDD.");
      return;
    }

    onConfirmar(telefoneNumeros);
  };

  const fecharModal = () => {
    setTelefone("");
    setErro("");
    onClose();
  };

  return (
    <div
      className="modal-telefone-fundo"
      onClick={fecharModal}
    >
      <div
        className="modal-telefone-container"
        onClick={(event) => event.stopPropagation()}
      >
        <button
          type="button"
          className="modal-telefone-fechar"
          onClick={fecharModal}
        >
          ×
        </button>

        <div className="modal-telefone-icone">☎</div>

        <h2>Receber pedido no WhatsApp</h2>

        <p>
          Informe seu número com DDD para receber os detalhes
          do pedido.
        </p>

        <label htmlFor="telefone">
          Número de telefone
        </label>

        <input
          id="telefone"
          type="tel"
          placeholder="(47) 99999-9999"
          value={telefone}
          onChange={alterarTelefone}
          autoFocus
        />

        {erro && (
          <span className="modal-telefone-erro">
            {erro}
          </span>
        )}

        <div className="modal-telefone-total">
          <span>Total do pedido</span>

          <strong>
            {total.toLocaleString("pt-BR", {
              style: "currency",
              currency: "BRL",
            })}
          </strong>
        </div>

        <div className="modal-telefone-botoes">
          <button
            type="button"
            className="botao-telefone-cancelar"
            onClick={fecharModal}
          >
            Cancelar
          </button>

          <button
            type="button"
            className="botao-telefone-confirmar"
            onClick={confirmarTelefone}
          >
            Abrir WhatsApp
          </button>
        </div>
      </div>
    </div>
  );
}

export default ModalTelefone;