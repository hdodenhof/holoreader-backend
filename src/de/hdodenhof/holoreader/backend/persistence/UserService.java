package de.hdodenhof.holoreader.backend.persistence;

public class UserService {

    public void register(UserEntity user) {
        // TODO check if user exists

        UserDao userDao = new UserDao();
        userDao.persist(user);
    }

    public UserEntity get(String eMail) {
        UserDao userDao = new UserDao();
        return userDao.findByEmail(eMail);
    }

}
