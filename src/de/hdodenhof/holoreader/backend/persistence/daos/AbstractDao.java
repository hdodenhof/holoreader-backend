package de.hdodenhof.holoreader.backend.persistence.daos;

import java.lang.reflect.ParameterizedType;

import javax.persistence.EntityManager;

import com.google.appengine.api.datastore.Key;

import de.hdodenhof.holoreader.backend.persistence.EMF;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractDao<E> {

    protected Class entityClass;
    protected EntityManager entityManager;

    public AbstractDao() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        this.entityClass = (Class) genericSuperclass.getActualTypeArguments()[0];
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

    public E load(Key key) {
        return (E) entityManager.find(entityClass, key);
    }

}
