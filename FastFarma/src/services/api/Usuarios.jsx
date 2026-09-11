export const BuscarUsuarios = async () => {
  const res = await fetch("http://localhost:8080/api/usuarios");

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao buscar usuarios");
  }

  return data;
};

