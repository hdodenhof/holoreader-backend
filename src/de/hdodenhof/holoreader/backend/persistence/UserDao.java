package de.hdodenhof.holoreader.backend.persistence;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class UserDao {

    protected EntityManager entityManager;

    public UserDao() {
        this.entityManager = EMF.get().createEntityManager();
    }

    public UserEntity findByEmail(String eMail) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.eMail = :email").setParameter("email", eMail);

        UserEntity user;
        try {
            user = (UserEntity) query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }

        return user;
    }

    public UserEntity findByRegistrationId(String regId) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.regId = :regid").setParameter("regid", regId);

        UserEntity user;
        try {
            user = (UserEntity) query.getSingleResult();
        } catch (NoResultException e) {
            user = null;
        }

        return user;
    }

    public void persist(UserEntity entity) {
        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
    }

    public void remove(UserEntity entity) {
        entityManager.getTransaction().begin();
        entityManager.remove(entity);
        entityManager.getTransaction().commit();
    }

}
