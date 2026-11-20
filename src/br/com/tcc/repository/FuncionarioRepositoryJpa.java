package br.com.tcc.repository;

import br.com.tcc.entity.Funcionario;
import java.util.List;
import javax.persistence.EntityManager;

public class FuncionarioRepositoryJpa implements FuncionarioRepository {

    private final EntityManager em;

    public FuncionarioRepositoryJpa(EntityManager em) {
        this.em = em;
    }

    @Override
    public Funcionario buscarPorLogin(String login) {
        List<Funcionario> lista = em
                .createQuery("SELECT f FROM Funcionario f WHERE f.login = :login", Funcionario.class)
                .setParameter("login", login)
                .getResultList();
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public void atualizar(Funcionario funcionario) {
        em.getTransaction().begin();
        try {
            em.merge(funcionario);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
