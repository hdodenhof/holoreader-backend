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

    public void register(String eMail, String model, String regId, String uuid) {
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

        boolean uuidExists = false;
        device = deviceDao.findByUuid(uuid);
        if (device != null) {
            uuidExists = true;
            deviceDao.remove(device);
        }

        device = new DeviceEntity();
        device.setDevice(model);
        device.setRegId(regId);
        device.setUuid(uuid);

        user.getDevices().add(device);
        userDao.persist(user);

        if (!uuidExists) {
            notifyAccountActivation(user, model);
        }
    }

    public UserEntity storeDummyUser(String eMail) {
        UserEntity user = new UserEntity();
        user.seteMail(eMail);

        UserDao userDao = new UserDao();
        userDao.persist(user);
        return user;
    }

    private void notifyAccountActivation(UserEntity user, String device) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        String msgBody = "Hello!\n\nHolo Reader FeedPusher (beta) has been enabled for your " + device + ".\n"
                + "You can now go to https://holoreader.appspot.com and send feeds to your device.\n\n"
                + "Please keep in mind that this service is still in its testing stage. I appreciate "
                + "any feedback via holoreader@hdodenhof.de.\n\nThanks!\n\n\n"
                + "P.S. There is a quota limit for using this service as long as it's free. The quota "
                + "is reset every 24 hours, so if you can't reach the service, please try again later.";

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("noreply@holoreader.appspotmail.com", "Holo Reader"));
            msg.setReplyTo(new Address[] { new InternetAddress("holoreader@hdodenhof.de") });
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.geteMail()));
            msg.setSubject("Holo Reader FeedPusher (beta)");
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

    public void updateRegId(String oldRegId, String newRegId) {
        DeviceDao deviceDao = new DeviceDao();
        DeviceEntity deviceOldRegId = deviceDao.findByRegistrationId(oldRegId);
        DeviceEntity deviceNewRegId = deviceDao.findByRegistrationId(newRegId);

        if (deviceOldRegId != null) {
            if (deviceNewRegId != null) {
                deviceDao.remove(deviceNewRegId);
            }

            deviceOldRegId.setRegId(newRegId);
            deviceDao.update(deviceOldRegId);
        }
    }

    public void removeDevice(String regId) {
        DeviceDao deviceDao = new DeviceDao();
        DeviceEntity device = deviceDao.findByRegistrationId(regId);
        if (device != null) {
            deviceDao.remove(device);
        }
    }
}
