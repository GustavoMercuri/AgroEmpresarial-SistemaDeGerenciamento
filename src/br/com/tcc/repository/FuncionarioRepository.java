package br.com.tcc.repository;

import br.com.tcc.entity.Funcionario;

public interface FuncionarioRepository {

    Funcionario buscarPorLogin(String login);

    void atualizar(Funcionario funcionario);
}
