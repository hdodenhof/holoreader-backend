package de.hdodenhof.holoreader.backend.persistence.daos;

import javax.persistence.EntityManager;

import de.hdodenhof.holoreader.backend.persistence.EMF;

public class AbstractDao<E> {

    protected EntityManager entityManager;

    public AbstractDao() {
        this.entityManager = EMF.get().createEntityManager();
    }

    public void persist(E entity) {
        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
    }

    public void remove(E entity) {
        entityManager.getTransaction().begin();
        entityManager.remove(entity);
        entityManager.getTransaction().commit();
    }

}
