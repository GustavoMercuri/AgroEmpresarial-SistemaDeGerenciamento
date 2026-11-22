package br.com.tcc.service;

import br.com.tcc.entity.Bairro;
import br.com.tcc.entity.Cliente;
import br.com.tcc.repository.ClienteRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Regras do cadastro de cliente, usadas pela ClienteView e pela
 * ClienteFuncionarioView (antes cada tela tinha a sua cópia do código).
 */
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Cliente> pesquisar(String rotuloOrdem, String texto) {
        return repositorio.pesquisar(new FiltroCliente(rotuloOrdem, texto));
    }

    /**
     * O bairro é recebido da tela e atribuído aqui. No legado ele dependia do
     * Beans Binding, que não copiava a seleção do combo para o cliente novo
     * e o INSERT falhava com "Cliente.idBairro" nulo.
     */
    public void salvar(Cliente cliente, Bairro bairroSelecionado) {
        if (cliente == null) {
            throw new RegraNegocioException("Nenhum cliente selecionado");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new RegraNegocioException("Nome deve ser informado");
        }
        if (bairroSelecionado == null) {
            throw new RegraNegocioException("Selecione um bairro");
        }
        validarNascimento(cliente.getDataNascimento());
        boolean novo = cliente.getId() == null;
        if (novo && temDigitos(cliente.getCpf()) && !CpfUtil.valido(cliente.getCpf())) {
            throw new RegraNegocioException("CPF inválido");
        }

        cliente.setIdBairro(bairroSelecionado);
        if (cliente.getClienteDesde() == null) {
            // o legado sobrescrevia essa data a cada edição
            cliente.setClienteDesde(new Date());
        }
        repositorio.salvar(cliente);
    }

    private void validarNascimento(Date nascimento) {
        if (nascimento == null) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(nascimento);
        if (cal.get(Calendar.YEAR) < 1900 || nascimento.after(new Date())) {
            throw new RegraNegocioException("Data de nascimento inválida");
        }
    }

    private static boolean temDigitos(String valor) {
        return valor != null && valor.replaceAll("\\D", "").length() > 0;
    }
}
