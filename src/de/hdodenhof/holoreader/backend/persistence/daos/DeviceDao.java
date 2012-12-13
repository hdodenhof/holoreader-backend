package de.hdodenhof.holoreader.backend.persistence.daos;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import de.hdodenhof.holoreader.backend.persistence.entities.DeviceEntity;

public class DeviceDao extends AbstractDao<DeviceEntity> {

    public DeviceEntity findByRegistrationId(String regId) {
        Query query = entityManager.createQuery("SELECT d FROM Device d WHERE d.regId = :regid").setParameter("regid", regId);

        DeviceEntity device;
        try {
            device = (DeviceEntity) query.getSingleResult();
        } catch (NoResultException e) {
            device = null;
        }

        return device;
    }

}
