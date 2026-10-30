import { useEffect, useId, useRef } from "react";
import "./ModalPedido.css";

const moeda = (valor) => Number(valor ?? 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const data = (valor) => {
    if (!valor) return "-";
    const instante = new Date(valor);
    return Number.isNaN(instante.getTime()) ? "-" : instante.toLocaleString("pt-BR");
};

/** Compartilha a visualização; ações administrativas são fornecidas apenas pelo painel. */
export default function ModalPedido({ pedido, onClose, bloqueado = false, analisar = false, children, acoes }) {
    const dialogRef = useRef(null);
    const tituloId = useId();

    useEffect(() => {
        const dialog = dialogRef.current;
        dialog.showModal();
        const overflowAnterior = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        return () => {
            dialog.close();
            document.body.style.overflow = overflowAnterior;
        };
    }, [pedido.id]);

    const fechar = () => { if (!bloqueado) onClose(); };

    return (
        <dialog ref={dialogRef} className="modal-pedido-container" aria-labelledby={tituloId}
            onCancel={(event) => { event.preventDefault(); fechar(); }}
            onClick={(event) => {
                if (event.target !== event.currentTarget) return;
                const limites = event.currentTarget.getBoundingClientRect();
                if (event.clientX < limites.left || event.clientX > limites.right ||
                    event.clientY < limites.top || event.clientY > limites.bottom) fechar();
            }}>
            <div className="modal-pedido-header">
                <div>
                    <h2 id={tituloId}>{analisar ? "Analisar" : "Pedido"} #{pedido.id}</h2>
                    <p>Cliente: {pedido.criadoPor}</p>
                </div>
                <button type="button" className="modal-pedido-fechar" onClick={fechar} disabled={bloqueado} aria-label="Fechar detalhes do pedido">×</button>
            </div>
            <div className="modal-pedido-info">
                <div><span>Status</span><strong className={`badge-status ${(pedido.status || "pendente").toLowerCase()}`}>{pedido.status || "PENDENTE"}</strong></div>
                <div><span>Código de retirada</span><strong>{pedido.codigoVerificacao ?? "Aguardando"}</strong></div>
                <div><span>Criado em</span><strong>{data(pedido.criadoEm)}</strong></div>
                <div><span>Atualizado em</span><strong>{data(pedido.atualizadoEm)}</strong></div>
            </div>
            <div className="modal-pedido-tabela">
                <table className="modal-pedido-itens">
                    <thead><tr><th>Produto</th><th>Preço</th><th>Qtd</th><th>Subtotal</th></tr></thead>
                    <tbody>
                        {(pedido.itens ?? []).map((item, indice) => (
                            <tr key={item.produtoId ?? indice}>
                                <td>{item.nome}</td><td>{moeda(item.precoUnitario)}</td><td>{item.quantidade ?? 1}</td>
                                <td>{moeda(item.subtotal ?? Number(item.precoUnitario ?? 0) * (item.quantidade ?? 1))}</td>
                            </tr>
                        ))}
                    </tbody>
                    <tfoot><tr><td colSpan="3">Total</td><td>{moeda(pedido.valorTotal)}</td></tr></tfoot>
                </table>
            </div>
            {children}
            <div className="modal-pedido-botoes">
                <button type="button" className="botao-pedido-fechar" onClick={fechar} disabled={bloqueado}>Fechar</button>
                {acoes}
            </div>
        </dialog>
    );
}
