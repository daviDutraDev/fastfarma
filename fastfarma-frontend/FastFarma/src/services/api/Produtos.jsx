export const BuscarProdutos = async () => {
  const res = await fetch("http://localhost:8080/api/produtos");

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao buscar produtos");
  }

  return data;
};

