package de.hdodenhof.holoreader.backend.persistence.services;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import de.hdodenhof.holoreader.backend.persistence.daos.DeviceDao;
import de.hdodenhof.holoreader.backend.persistence.daos.UserDao;
import de.hdodenhof.holoreader.backend.persistence.entities.DeviceEntity;
import de.hdodenhof.holoreader.backend.persistence.entities.UserEntity;

public class UserAndDeviceService {

    public void register(String eMail, String model, String regId) {
        UserDao userDao = new UserDao();
        UserEntity user = userDao.findByEmail(eMail);
        if (user == null) {
            user = new UserEntity();
            user.seteMail(eMail);
        }

        DeviceDao deviceDao = new DeviceDao();
        DeviceEntity device = deviceDao.findByRegistrationId(regId);
        if (device != null) {
            deviceDao.remove(device);
        }
        device = new DeviceEntity();
        device.setDevice(model);
        device.setRegId(regId);

        user.getDevices().add(device);
        userDao.persist(user);
    }

    public UserEntity storeDummyUser(String eMail) {
        UserEntity user = new UserEntity();
        user.seteMail(eMail);

        UserDao userDao = new UserDao();
        userDao.persist(user);
        return user;
    }

    public UserEntity get(String eMail) {
        UserDao userDao = new UserDao();
        return userDao.findByEmail(eMail);
    }

    public DeviceEntity loadDevice(String keyString) {
        Key key = KeyFactory.stringToKey(keyString);
        DeviceDao deviceDao = new DeviceDao();
        return deviceDao.load(key);
    }

}
