package br.com.tcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FiltroClienteTest {

    @Test
    void apostrofoVaiComoParametroENaoDentroDaConsulta() {
        // no legado: "unexpected token: VILA" e a lista não era atualizada
        FiltroCliente filtro = new FiltroCliente("Nome", "D'Ávila");

        assertEquals("SELECT c FROM Cliente c WHERE UPPER(c.nome) LIKE :filtro ORDER BY c.nome", filtro.jpql());
        assertFalse(filtro.jpql().contains("'"));
        assertEquals("%D'ÁVILA%", filtro.valorParametro());
    }

    @Test
    void textoMaliciosoNaoAlteraAConsulta() {
        FiltroCliente filtro = new FiltroCliente("Nome", "x' OR '1'='1");

        assertEquals("SELECT c FROM Cliente c WHERE UPPER(c.nome) LIKE :filtro ORDER BY c.nome", filtro.jpql());
    }

    @Test
    void campoDeOrdemDesconhecidoCaiNoCodigo() {
        FiltroCliente filtro = new FiltroCliente("nome); DROP TABLE cliente; --", "");

        assertEquals("SELECT c FROM Cliente c ORDER BY c.id", filtro.jpql());
    }

    @Test
    void ordemPorCodigoFiltraPeloNome() {
        // no legado, com a ordem em "Código", buscar "Jarbas" não achava nada
        FiltroCliente filtro = new FiltroCliente("Código", "Jarbas");

        assertEquals("nome", filtro.campoFiltro());
        assertEquals("SELECT c FROM Cliente c WHERE UPPER(c.nome) LIKE :filtro ORDER BY c.id", filtro.jpql());
    }

    @Test
    void filtroVazioOuSoComEspacosListaTodos() {
        assertFalse(new FiltroCliente("Nome", "   ").temFiltro());
        assertFalse(new FiltroCliente("Nome", null).temFiltro());
        assertTrue(new FiltroCliente("Nome", " Caio ").temFiltro());
        assertEquals("%CAIO%", new FiltroCliente("Nome", " Caio ").valorParametro());
    }
}
