export const BuscarProdutos = async () => {
  const res = await fetch("http://localhost:8080/api/produtos");

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao buscar produtos");
  }

  return data;
};

export const cadastrarProduto = async (nome, preco,estoque, categoria ) => {
  const res = await fetch("http://localhost:8080/api/produtos", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        nome,
        preco,
        estoque,
        categoria
      }),
    });
  
    const data = await res.json();
  
    if (!res.ok) {
      throw new Error(data.mensagem || "Erro ao cadastrar produto");
    }
    return data;
}

export const deletarProduto = async (id) => {
  const res = await fetch(`http://localhost:8080/api/produtos/${id}`, {
      method: "DELETE"});
  
    const data = await res.json();
  
    if (!res.ok) {
      throw new Error(data.mensagem || "Erro ao deletar produto");
    }
    return data;
}



