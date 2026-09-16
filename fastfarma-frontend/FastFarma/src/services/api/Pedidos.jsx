export const BuscarPedidos = async () => {
  const res = await fetch("http://localhost:8080/api/pedidos");

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao buscar pedidos");
  }

  return data;
};

export const BuscarPedidosUsuario = async (nome) => {
  const res = await fetch(`http://localhost:8080/api/pedidos/cliente/${nome}`);

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao buscar pedidos");
  }

  return data;
};