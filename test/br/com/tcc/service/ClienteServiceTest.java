package br.com.tcc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.tcc.entity.Bairro;
import br.com.tcc.entity.Cliente;
import br.com.tcc.repository.ClienteRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClienteServiceTest {

    static class RepositorioFalso implements ClienteRepository {
        final List<Cliente> salvos = new ArrayList<>();
        FiltroCliente ultimoFiltro;

        @Override
        public List<Cliente> pesquisar(FiltroCliente filtro) {
            ultimoFiltro = filtro;
            return new ArrayList<>();
        }

        @Override
        public void salvar(Cliente cliente) {
            salvos.add(cliente);
        }
    }

    private RepositorioFalso repositorio;
    private ClienteService servico;
    private Bairro castilho;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioFalso();
        servico = new ClienteService(repositorio);
        castilho = new Bairro();
        castilho.setNome("Castilho");
    }

    private Cliente clienteNovo(String nome) {
        Cliente c = new Cliente();
        c.setNome(nome);
        return c;
    }

    private Date data(int ano, int mes, int dia) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(ano, mes - 1, dia);
        return cal.getTime();
    }

    @Test
    void clienteNovoRecebeOBairroEscolhidoNaTela() {
        // regressão do erro "not-null property references a null value: Cliente.idBairro"
        Cliente gustavo = clienteNovo("Gustavo Mercuri");

        servico.salvar(gustavo, castilho);

        assertSame(castilho, gustavo.getIdBairro());
        assertEquals(1, repositorio.salvos.size());
    }

    @Test
    void naoGravaSemBairroNemSemNome() {
        assertThrows(RegraNegocioException.class, () -> servico.salvar(clienteNovo("Gustavo"), null));
        assertThrows(RegraNegocioException.class, () -> servico.salvar(clienteNovo("  "), castilho));
        assertTrue(repositorio.salvos.isEmpty());
    }

    @Test
    void recusaDataDeNascimentoImpossivel() {
        // o banco de exemplo tem o Jarbas nascido no ano 3
        Cliente jarbas = clienteNovo("Jarbas");
        jarbas.setDataNascimento(data(3, 2, 1));

        assertThrows(RegraNegocioException.class, () -> servico.salvar(jarbas, castilho));
    }

    @Test
    void recusaCpfInvalidoEmClienteNovo() {
        Cliente c = clienteNovo("Fulano");
        c.setCpf("123.456.789-10");

        assertThrows(RegraNegocioException.class, () -> servico.salvar(c, castilho));
    }

    @Test
    void aceitaCpfValidoOuNaoInformado() {
        Cliente comCpf = clienteNovo("Fulano");
        comCpf.setCpf("529.982.247-25");
        Cliente semCpf = clienteNovo("Beltrano");
        semCpf.setCpf("   .   .   -  "); // máscara vazia do campo

        servico.salvar(comCpf, castilho);
        servico.salvar(semCpf, castilho);

        assertEquals(2, repositorio.salvos.size());
    }

    @Test
    void edicaoNaoApagaADataDeClienteDesde() {
        // o legado trocava "Cliente desde" pela data de hoje a cada edição
        Cliente antigo = clienteNovo("Caio");
        antigo.setId(31);
        Date desde = data(2015, 10, 5);
        antigo.setClienteDesde(desde);

        servico.salvar(antigo, castilho);

        assertEquals(desde, antigo.getClienteDesde());
    }

    @Test
    void clienteNovoGanhaDataDeCadastro() {
        Cliente novo = clienteNovo("Novo");

        servico.salvar(novo, castilho);

        assertNotNull(novo.getClienteDesde());
    }

    @Test
    void pesquisaRepassaOFiltroMontado() {
        servico.pesquisar("Nome", "D'Ávila");

        assertEquals("D'ÁVILA%", repositorio.ultimoFiltro.valorParametro());
    }

    @Test
    void validacaoDeCpf() {
        assertTrue(CpfUtil.valido("529.982.247-25"));
        assertTrue(CpfUtil.valido("52998224725"));
        assertTrue(!CpfUtil.valido("888.888.888-88"));
        assertTrue(!CpfUtil.valido("123.456.789-10"));
        assertTrue(!CpfUtil.valido("123"));
    }
}
