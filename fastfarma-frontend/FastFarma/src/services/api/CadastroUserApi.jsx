export const CadastrarUsuario = async (nome, email, senha) => {
  const res = await fetch("http://localhost:8080/api/auth/cadastrar", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      nome,
      email,
      senha,
    }),
  });

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "Erro ao cadastrar usuário");
  }

  return data;
};