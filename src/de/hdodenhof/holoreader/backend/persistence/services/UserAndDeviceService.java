package de.hdodenhof.holoreader.backend.persistence.services;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
            notifyAccountActivation(user);
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

    private void notifyAccountActivation(UserEntity user) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        String msgBody = "Hello!\n\nHolo Reader FeedToDevice has been enabled for your account.\n"
                + "You can now go to https://holoreader.appspot.com and send feeds to your device(s).\n\n"
                + "Please contact holoreader@hdodenhof.de if you have any issue.\n\n" + "Thanks\n" + "Henning";

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("noreply@holoreader.appspotmail.com", "Holo Reader"));
            msg.setReplyTo(new Address[] { new InternetAddress("holoreader@hdodenhof.de") });
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.geteMail()));
            msg.setSubject("Holo Reader FeedToDevice");
            msg.setText(msgBody);
            Transport.send(msg);
        } catch (Exception e) {
        }
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
