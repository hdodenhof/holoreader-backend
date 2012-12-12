package de.hdodenhof.holoreader.backend.persistence;

public class UserEntityService {

    public void register(String eMail, String regId) {
        // TODO check if user exists
        UserEntity user = new UserEntity();
        user.seteMail(eMail);
        user.setRegId(regId);
        user.setDevice("");

        UserDao userDao = new UserDao();
        userDao.persist(user);
    }

    public void unregister(String regId) {
        UserDao userDao = new UserDao();
        UserEntity user = userDao.findByRegistrationId(regId);
        if (user != null) {
            userDao.remove(user);
        }
    }

    public UserEntity get(String eMail) {
        UserDao userDao = new UserDao();
        return userDao.findByEmail(eMail);
    }

}
