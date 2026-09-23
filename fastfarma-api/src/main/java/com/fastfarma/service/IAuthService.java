package com.fastfarma.service;

import com.fastfarma.dto.AtualizarPerfilRequest;
import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.TrocarSenhaRequest;
import com.fastfarma.dto.UsuarioResponse;

import java.util.List;

/**
 * Contrato do serviço de autenticação / cadastro de usuários.
 * Aplicado o Princípio da Inversão de Dependência.
 */
public interface IAuthService {

    /** Tenta autenticar — retorna {@code null} se as credenciais forem inválidas. */
    LoginResponse login(LoginRequest request);

    /** Cadastra um novo cliente (sempre do tipo {@code CLIENTE}). */
    UsuarioResponse cadastrar(CadastroRequest request);

    /** Lista todos os usuários cadastrados. */
    List<UsuarioResponse> listarTodos();

    /** Busca um usuário pelo id — lança exceção se não existir. */
    UsuarioResponse buscarPorId(Integer id);

    /** Busca um usuário pelo nome — lança exceção se não existir. */
    UsuarioResponse buscarPorNome(String nome);

    /** Atualiza nome e telefone do usuario identificado pelo nome. */
    UsuarioResponse atualizarPerfil(String nome, AtualizarPerfilRequest request);

    /** Troca a senha (exige senhaAtual correta). */
    void trocarSenha(String nome, TrocarSenhaRequest request);

    /** Exclui um usuário pelo id — lança exceção se não existir. */
    void excluir(Integer id);
}
