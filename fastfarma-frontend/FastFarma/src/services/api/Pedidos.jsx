export const BuscarPedidos = async () => {
  const res = await fetch("http://localhost:8080/api/pedidos");

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao buscar pedidos");
  }

  return data;
};