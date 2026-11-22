package br.com.tcc.repository;

import br.com.tcc.entity.Cliente;
import br.com.tcc.service.FiltroCliente;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class ClienteRepositoryJpa implements ClienteRepository {

    private final EntityManager em;

    public ClienteRepositoryJpa(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Cliente> pesquisar(FiltroCliente filtro) {
        TypedQuery<Cliente> consulta = em.createQuery(filtro.jpql(), Cliente.class);
        if (filtro.temFiltro()) {
            consulta.setParameter("filtro", filtro.valorParametro());
        }
        return consulta.getResultList();
    }

    @Override
    public void salvar(Cliente cliente) {
        em.getTransaction().begin();
        try {
            if (cliente.getId() == null) {
                em.persist(cliente);
            } else {
                em.merge(cliente);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
