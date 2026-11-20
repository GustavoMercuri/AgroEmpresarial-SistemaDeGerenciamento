package br.com.tcc.service;

import br.com.tcc.entity.Funcionario;
import br.com.tcc.repository.FuncionarioRepository;

/**
 * Regra de login que antes ficava dentro da LoginView.
 *
 * Antes: a tela gerava o MD5 da senha digitada e buscava
 * "login = ? AND senha = ?" direto no banco.
 * Agora: busca pelo login, confere a senha com SenhaService e,
 * se o hash gravado ainda for MD5, troca por PBKDF2 na hora.
 */
public class AutenticacaoService {

    private final FuncionarioRepository repositorio;

    public AutenticacaoService(FuncionarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** @return o funcionário autenticado, ou null se login/senha não conferem */
    public Funcionario autenticar(String login, String senha) {
        if (login == null || login.trim().isEmpty() || senha == null || senha.isEmpty()) {
            return null;
        }
        Funcionario funcionario = repositorio.buscarPorLogin(login.trim());
        if (funcionario == null || !SenhaService.confere(senha, funcionario.getSenha())) {
            return null;
        }
        if (!SenhaService.ehHashSeguro(funcionario.getSenha())) {
            funcionario.setSenha(SenhaService.gerarHash(senha));
            repositorio.atualizar(funcionario);
        }
        return funcionario;
    }
}
