package br.com.tcc.repository;

import br.com.tcc.entity.Cliente;
import br.com.tcc.service.FiltroCliente;
import java.util.List;

public interface ClienteRepository {

    List<Cliente> pesquisar(FiltroCliente filtro);

    void salvar(Cliente cliente);
}
