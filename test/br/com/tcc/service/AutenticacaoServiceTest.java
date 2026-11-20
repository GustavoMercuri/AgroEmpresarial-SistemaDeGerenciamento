package br.com.tcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.tcc.entity.Funcionario;
import br.com.tcc.repository.FuncionarioRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticacaoServiceTest {

    /** Repositório em memória: o teste não precisa de MySQL. */
    static class RepositorioFalso implements FuncionarioRepository {
        final Map<String, Funcionario> porLogin = new HashMap<>();
        int atualizacoes = 0;

        @Override
        public Funcionario buscarPorLogin(String login) {
            return porLogin.get(login);
        }

        @Override
        public void atualizar(Funcionario funcionario) {
            atualizacoes++;
        }
    }

    private RepositorioFalso repositorio;
    private AutenticacaoService servico;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        servico = new AutenticacaoService(repositorio);
    }

    private Funcionario funcionario(String login, String senhaGravada) {
        Funcionario f = new Funcionario();
        f.setId(1);
        f.setLogin(login);
        f.setSenha(senhaGravada);
        repositorio.porLogin.put(login, f);
        return f;
    }

    @Test
    void loginComSenhaLegadaFuncionaEMigraParaPbkdf2() {
        Funcionario leonardo = funcionario("Leonardo", "ICy5YqxZB1uWSwcVLSNLcA==");

        Funcionario logado = servico.autenticar("Leonardo", "123");

        assertSame(leonardo, logado);
        assertTrue(SenhaService.ehHashSeguro(leonardo.getSenha()));
        assertTrue(SenhaService.confere("123", leonardo.getSenha()));
        assertEquals(1, repositorio.atualizacoes);
    }

    @Test
    void loginComHashNovoNaoPrecisaAtualizarOBanco() {
        funcionario("Aluno", SenhaService.gerarHash("123"));

        assertNotNull(servico.autenticar("Aluno", "123"));
        assertEquals(0, repositorio.atualizacoes);
    }

    @Test
    void senhaErradaNaoAutenticaENaoMexeNoHash() {
        Funcionario aluno = funcionario("Aluno", "ICy5YqxZB1uWSwcVLSNLcA==");

        assertNull(servico.autenticar("Aluno", "errada"));
        assertFalse(SenhaService.ehHashSeguro(aluno.getSenha()));
        assertEquals(0, repositorio.atualizacoes);
    }

    @Test
    void loginInexistenteOuCamposVaziosNaoAutenticam() {
        assertNull(servico.autenticar("ninguem", "123"));
        assertNull(servico.autenticar("", "123"));
        assertNull(servico.autenticar("Aluno", ""));
        assertNull(servico.autenticar(null, null));
    }
}
