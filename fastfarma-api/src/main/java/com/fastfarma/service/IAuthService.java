package com.fastfarma.service;

import com.fastfarma.dto.CadastroRequest;
import com.fastfarma.dto.LoginRequest;
import com.fastfarma.dto.LoginResponse;
import com.fastfarma.dto.UsuarioResponse;

/**
 * Contrato do serviço de autenticação / cadastro de usuários.
 * Aplicado o Princípio da Inversão de Dependência.
 */
public interface IAuthService {

    /** Tenta autenticar — retorna {@code null} se as credenciais forem inválidas. */
    LoginResponse login(LoginRequest request);

    /** Cadastra um novo cliente (sempre do tipo {@code CLIENTE}). */
    UsuarioResponse cadastrar(CadastroRequest request);

    /** Busca um usuário pelo id — lança exceção se não existir. */
    UsuarioResponse buscarPorId(Integer id);
}
