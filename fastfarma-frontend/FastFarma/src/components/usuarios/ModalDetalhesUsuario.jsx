import { useEffect, useRef, useState } from "react";
import { FiX } from "react-icons/fi";
import { BuscarPedidosUsuario } from "../../services/api/Pedidos";
import "./ModalDetalhesUsuario.css";

const LIMITE_RECENTES = 3;
const STATUS_PEDIDO = {
  PENDENTE: "Pendente",
  APROVADO: "Aprovado",
  REJEITADO: "Rejeitado",
  PRONTO: "Pronto para retirada",
};

function formatarData(valor) {
  if (!valor) return "Não informada";
  const data = new Date(valor);
  return Number.isNaN(data.getTime()) ? "Não informada" : data.toLocaleDateString("pt-BR");
}

function formatarTelefone(valor) {
  if (!valor) return "Não informado";
  return valor.replace(/^(\d{2})(\d{4,5})(\d{4})$/, "($1) $2-$3");
}

function ModalDetalhesUsuario({ usuario, nomeDuplicado, onClose }) {
  const dialogRef = useRef(null);
  const [pedidos, setPedidos] = useState(null);
  const [erro, setErro] = useState(false);
  const [mostrarTodos, setMostrarTodos] = useState(false);
  const cliente = usuario.tipo === "CLIENTE";
  const perfil = cliente ? "Cliente" : "Funcionário";
  const nome = usuario.nome;
  const iniciais = nome.trim().split(/\s+/).slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase()).join("");

  useEffect(() => {
    const dialog = dialogRef.current;
    dialog.showModal();
    const overflowAnterior = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      dialog.close();
      document.body.style.overflow = overflowAnterior;
    };
  }, []);

  useEffect(() => {
    if (!cliente || nomeDuplicado) return;
    let ativo = true;

    BuscarPedidosUsuario(nome)
      .then(({ dados }) => {
        if (ativo) setPedidos(Array.isArray(dados) ? dados : []);
      })
      .catch(() => {
        if (ativo) setErro(true);
      });

    return () => { ativo = false; };
  }, [cliente, nomeDuplicado, nome]);

  const pedidosVisiveis = mostrarTodos ? pedidos : pedidos?.slice(0, LIMITE_RECENTES);

  return (
    <dialog
      ref={dialogRef}
      className="detalhes-usuario-modal"
      aria-labelledby="detalhes-usuario-titulo"
      onCancel={(event) => {
        event.preventDefault();
        onClose();
      }}
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div className="detalhes-usuario-conteudo">
        <header className="detalhes-usuario-cabecalho">
          <h2 id="detalhes-usuario-titulo">Detalhes do {perfil.toLowerCase()}</h2>
          <button type="button" className="detalhes-usuario-fechar" onClick={onClose} aria-label="Fechar detalhes">
            <FiX aria-hidden="true" />
          </button>
        </header>

        <div className="detalhes-usuario-identidade">
          <span className="detalhes-usuario-avatar" aria-hidden="true">{iniciais}</span>
          <div>
            <h3>{nome}</h3>
            <p>{perfil} #{usuario.id}</p>
          </div>
        </div>

        <dl className="detalhes-usuario-dados">
          <div><dt>E-mail</dt><dd>{usuario.email}</dd></div>
          <div><dt>Telefone</dt><dd>{formatarTelefone(usuario.telefone)}</dd></div>
          <div><dt>Cadastro</dt><dd>{formatarData(usuario.criadoEm)}</dd></div>
        </dl>

        {cliente ? (
          <section className="detalhes-usuario-secao">
            <h3>{mostrarTodos ? "Pedidos do cliente" : "Pedidos recentes"}</h3>
            {nomeDuplicado && <p>Há usuários com o mesmo nome. Consulte os pedidos pelo número do pedido.</p>}
            {erro && <p role="alert">Não foi possível carregar os pedidos.</p>}
            {!nomeDuplicado && !erro && pedidos === null && <p role="status">Carregando pedidos…</p>}
            {pedidos?.length === 0 && <p>Nenhum pedido encontrado.</p>}
            {pedidosVisiveis?.length > 0 && (
              <ul className="detalhes-usuario-pedidos">
                {pedidosVisiveis.map((pedido) => (
                  <li key={pedido.id}>
                    <strong>#{pedido.id}</strong>
                    <span>{formatarData(pedido.criadoEm)}</span>
                    <span className={`detalhes-usuario-selo ${pedido.status === "PRONTO" ? "pronto" : ""}`}>
                      {STATUS_PEDIDO[pedido.status] ?? pedido.status}
                    </span>
                  </li>
                ))}
              </ul>
            )}
            {pedidos?.length > LIMITE_RECENTES && (
              <button type="button" className="detalhes-usuario-ver-pedidos" onClick={() => setMostrarTodos(!mostrarTodos)}>
                {mostrarTodos ? "Ver apenas os recentes" : "Ver pedidos do cliente"}
              </button>
            )}
          </section>
        ) : (
          <section className="detalhes-usuario-secao">
            <h3>Perfil de acesso</h3>
            <span className="detalhes-usuario-selo">{perfil}</span>
          </section>
        )}
      </div>
    </dialog>
  );
}

export default ModalDetalhesUsuario;
