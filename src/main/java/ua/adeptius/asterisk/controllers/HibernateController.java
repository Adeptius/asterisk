package ua.adeptius.asterisk.controllers;


import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.model.User;

public class HibernateController {

    private static Logger LOGGER = LoggerFactory.getLogger(HibernateController.class.getSimpleName());

//    public static void saveNewUser(User user) throws Exception {
//        HibernateDao.saveUser(user);
//        UserContainer.putUser(user);
//    }

//    public static void updateUser(User user) throws Exception {
//        HibernateDao.update(user);
//    }

//    public static void removeTelephony(User user) throws Exception {
//        PhonesController.releaseAllTelephonyNumbers(user.getTelephony());
//        HibernateDao.removeTelephony(user);
//        CallProcessor.updatePhonesHashMap();
//    }

//    public static void removeTracking(User user) throws Exception {
//        PhonesController.releaseAllTrackingNumbers(user.getTracking());
//        HibernateDao.removeTracking(user);
//        CallProcessor.updatePhonesHashMap();
//    }

//    public static void removeAmoAccount(User user) throws Exception {
//        HibernateDao.removeAmoAccount(user);
//    }

//    public static void removeRoistatAccount(User user) throws Exception {
//        HibernateDao.removeRoistatAccount(user);
//    }

//    public static void removeUser(User user) throws Exception {
//        HibernateDao.deleteUser(user.getLogin());
//        PhonesController.releaseAllCustomerNumbers(user);
//        UserContainer.removeUser(user);
//        CallProcessor.updatePhonesHashMap();
//    }

}