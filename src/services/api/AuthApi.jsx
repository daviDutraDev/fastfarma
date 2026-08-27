export const FazerLogin = async (email, senha) => {
  const res = await fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      senha,
    }),
  });

  const data = await res.json();

  if (!res.ok) {
    throw new Error(data.mensagem || "E-mail ou senha inválidos");
  }

  return data;
};