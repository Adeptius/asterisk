package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class HibernateController {

    private static Logger LOGGER = LoggerFactory.getLogger(HibernateController.class.getSimpleName());

    @Autowired
    private HibernateDao hibernateDao;


    /**
     * User
     */

//    @Transactional
    public List<User> getAllUsers() throws Exception {
        return hibernateDao.getAllUsers();
    }

//    @Transactional
    public void saveUser(User user) throws Exception {
        hibernateDao.saveUser(user);
    }

//    @Transactional
    public void update(User user) {
        hibernateDao.update(user);
    }

//    @Transactional
    public User getUserByLogin(String login) throws Exception {
        long t0 = System.nanoTime();
        User userByLogin = hibernateDao.getUserByLogin(login);
        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0));
        return userByLogin;
    }


//    @Transactional
    public void delete(User user) {
        hibernateDao.delete(user);
    }

    /**
     * Melodies
     */

//    @Transactional
    public List<String> getMelodies() {
        return hibernateDao.getMelodies();
    }

    /**
     * AmoCRM
     */
    // Для тестов
//    @Transactional
    public AmoAccount getAmoAccountByUser(String nextelLogin) {
        return hibernateDao.getAmoAccountByUser(nextelLogin);
    }

    /**
     * Roistat
     */
    // Для тестов
//    @Transactional
    public RoistatAccount getRoistatAccountByUser(String nextelLogin) {
        return hibernateDao.getRoistatAccountByUser(nextelLogin);
    }

    /**
     * Inner phones
     */

//    @Transactional
    public List<InnerPhone> getAllInnerPhones() throws Exception {
        return hibernateDao.getAllInnerPhones();
    }

    @Deprecated
//    @Transactional
    public List<InnerPhone> getAllInnerUserPhones(String user) throws Exception {
        return hibernateDao.getAllInnerUserPhones(user);
    }

    @Deprecated
//    @Transactional
    public InnerPhone saveSipBySipConfig(SipConfig sipConfig, String user) throws Exception {
        return hibernateDao.saveSipBySipConfig(sipConfig, user);
    }


//    @Transactional
    public int getSipMaxNumber() throws Exception {
        return hibernateDao.getSipMaxNumber();
    }


    /**
     * Outer Phones
     */

//    @Transactional
    public List<OuterPhone> getAllFreeOuterPhones() throws Exception {
        return getAllFreeOuterPhones();
    }

    @Deprecated
//    @Transactional
    public List<OuterPhone> getAllBusyOuterPhones() throws Exception {
        return hibernateDao.getAllBusyOuterPhones();
    }


//    @Transactional
    public void markOuterPhoneBusy(String user, List<String> numbers) throws Exception {
        hibernateDao.markOuterPhoneBusy(user, numbers);
    }

//    @Transactional
    public void markOuterPhoneFree(List<String> numbersToRelease) throws Exception {
        hibernateDao.markOuterPhoneFree(numbersToRelease);
    }


    // Используется в тестах
//    @Transactional
    public List<OuterPhone> getAllTestPhones() throws Exception {
        return hibernateDao.getAllTestPhones();
    }

//    @Transactional
    public void removeAllTestPhones() throws Exception {
        hibernateDao.removeAllTestPhones();
    }

//    @Transactional
    public void createTestPhonesForUser(String user, String siteName) throws Exception {
        hibernateDao.createTestPhonesForUser(user, siteName);
    }

//    @Transactional
    public void delete(Object o) {
        hibernateDao.delete(o);
    }


}