package repository;

import model.TipoUsuario;
import model.Usuario;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    private String caminho = "src/data/usuarios.txt";

    // LISTAR
    public List<Usuario> listarUsuarios() {

        List<Usuario> usuarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {

            String linha;

            while ((linha = br.readLine()) != null) {

                if (linha.trim().isEmpty()) continue;

                String[] partes = linha.split(";");

                if (partes.length < 5) continue;

                int id = Integer.parseInt(partes[0]);
                String nome = partes[1];
                String email = partes[2];
                String senha = partes[3];
                TipoUsuario tipo = TipoUsuario.valueOf(partes[4]);

                usuarios.add(new Usuario(id, nome, email, senha, tipo));
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler usuários");
        }

        return usuarios;
    }

    // SALVAR (append)
    public void salvarUsuario(Usuario usuario) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho, true))) {

            String linha = usuario.getId() + ";"
                    + usuario.getNome() + ";"
                    + usuario.getEmail() + ";"
                    + usuario.getSenha() + ";"
                    + usuario.getTipo();

            bw.write(linha);
            bw.newLine();

        } catch (IOException e) {
            System.out.println("Erro ao salvar usuário");
        }
    }

    // LOGIN
    public Usuario login(String email, String senha) {

        List<Usuario> usuarios = listarUsuarios();

        for (Usuario u : usuarios) {
            if (u.getEmail().equals(email) && u.getSenha().equals(senha)) {
                return u;
            }
        }

        return null;
    }

    // GERAR ID
    public int gerarNovoId() {

        List<Usuario> usuarios = listarUsuarios();

        int maior = 0;

        for (Usuario u : usuarios) {
            if (u.getId() > maior) {
                maior = u.getId();
            }
        }

        return maior + 1;
    }

    // CRIAR ADMIN AUTOMATICAMENTE
    public void criarAdminSeNaoExistir() {

        List<Usuario> usuarios = listarUsuarios();

        if (usuarios.isEmpty()) {

            Usuario admin = new Usuario(
                    1,
                    "admin",
                    "admin@email.com",
                    "123",
                    TipoUsuario.FUNCIONARIO
            );

            salvarUsuario(admin);
        }
    }

    public String listarUsuariosFormatado() {

        List<Usuario> usuarios = listarUsuarios();

        StringBuilder sb = new StringBuilder();

        for (Usuario u : usuarios) {
            sb.append(u.getId())
                    .append(" - ")
                    .append(u.getNome())
                    .append(" - ")
                    .append(u.getEmail())
                    .append(" - ")
                    .append(u.getTipo())
                    .append("\n");
        }

        return sb.toString();
    }

    public void excluirUsuario(int id) {

        List<Usuario> usuarios = listarUsuarios();

        List<Usuario> novaLista = new ArrayList<>();

        for (Usuario u : usuarios) {
            if (u.getId() != id) {
                novaLista.add(u);
            }
        }

        // reescreve o arquivo
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho))) {

            for (Usuario u : novaLista) {

                String linha = u.getId() + ";"
                        + u.getNome() + ";"
                        + u.getEmail() + ";"
                        + u.getSenha() + ";"
                        + u.getTipo();

                bw.write(linha);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Erro ao excluir usuário");
        }
    }
}