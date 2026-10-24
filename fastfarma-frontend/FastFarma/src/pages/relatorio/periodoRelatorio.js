// Datas de calendário, sem converter meia-noite local para UTC.
export const formatarData = (data) => `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, "0")}-${String(data.getDate()).padStart(2, "0")}`;

export function obterPeriodo(opcao, hoje = new Date()) {
    if (opcao === "todo") return { inicio: "", fim: "" };
    const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
    if (opcao === "7dias") inicio.setDate(inicio.getDate() - 6);
    if (opcao === "30dias") inicio.setDate(inicio.getDate() - 29);
    if (opcao === "mes") inicio.setDate(1);
    return { inicio: formatarData(inicio), fim: formatarData(hoje) };
}
