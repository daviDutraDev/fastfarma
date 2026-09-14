export const atualizarEstoque = async (id, quantidade) => {
  const res = await fetch(`http://localhost:8080/api/estoque/adicionar/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        quantidade
      }),
    });
  
    const data = await res.json();
  
    if (!res.ok) {
      throw new Error(data.mensagem || "Erro ao atualizar estoque");
    }
    return data;
}
    