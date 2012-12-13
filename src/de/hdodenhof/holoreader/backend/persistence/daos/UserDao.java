package de.hdodenhof.holoreader.backend.persistence.daos;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import de.hdodenhof.holoreader.backend.persistence.entities.UserEntity;

public class UserDao extends AbstractDao<UserEntity> {

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

}
