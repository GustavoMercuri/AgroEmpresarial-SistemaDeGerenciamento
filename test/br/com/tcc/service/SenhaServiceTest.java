package br.com.tcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhaServiceTest {

    // hash que está no banco de exemplo para os usuários Leonardo, Fernando e Aluno (senha "123")
    private static final String MD5_DO_BANCO = "ICy5YqxZB1uWSwcVLSNLcA==";

    @Test
    void caracterizacao_md5LegadoGeraOMesmoValorQueOBancoDeExemplo() {
        // teste de caracterização: garante que o cálculo antigo foi reproduzido igual,
        // senão ninguém consegue logar depois da refatoração
        assertEquals(MD5_DO_BANCO, SenhaService.md5Legado("123"));
    }

    @Test
    void hashNovoUsaPbkdf2EConfereComASenhaCerta() {
        String hash = SenhaService.gerarHash("minhaSenha");

        assertTrue(hash.startsWith("pbkdf2$"));
        assertTrue(SenhaService.confere("minhaSenha", hash));
        assertFalse(SenhaService.confere("outraSenha", hash));
    }

    @Test
    void mesmaSenhaGeraHashesDiferentesPorCausaDoSalt() {
        assertNotEquals(SenhaService.gerarHash("123"), SenhaService.gerarHash("123"));
    }

    @Test
    void aceitaSenhaNoFormatoLegadoParaPermitirMigracao() {
        assertTrue(SenhaService.confere("123", MD5_DO_BANCO));
        assertFalse(SenhaService.confere("1234", MD5_DO_BANCO));
        assertFalse(SenhaService.ehHashSeguro(MD5_DO_BANCO));
    }

    @Test
    void senhaVaziaNaoGeraHash() {
        assertThrows(IllegalArgumentException.class, () -> SenhaService.gerarHash(""));
        assertThrows(IllegalArgumentException.class, () -> SenhaService.gerarHash(null));
    }

    @Test
    void nuncaConfereComValoresNulos() {
        assertFalse(SenhaService.confere(null, MD5_DO_BANCO));
        assertFalse(SenhaService.confere("123", null));
    }

    @Test
    void prepararParaGravarNaoGeraHashDeHashJaExistente() {
        // no legado, sair do campo senha gerava MD5 do MD5 a cada foco perdido
        String hash = SenhaService.gerarHash("123");

        assertEquals(hash, SenhaService.prepararParaGravar(hash));
        assertEquals(MD5_DO_BANCO, SenhaService.prepararParaGravar(MD5_DO_BANCO));
        assertTrue(SenhaService.confere("abc", SenhaService.prepararParaGravar("abc")));
    }
}
