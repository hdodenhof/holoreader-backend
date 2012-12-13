package de.hdodenhof.holoreader.backend.persistence.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.google.appengine.api.datastore.Key;

@Entity(name = "User")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
    private String eMail;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DeviceEntity> devices;

    public UserEntity() {
        devices = new ArrayList<DeviceEntity>();
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public List<DeviceEntity> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceEntity> devices) {
        this.devices = devices;
    }

}
