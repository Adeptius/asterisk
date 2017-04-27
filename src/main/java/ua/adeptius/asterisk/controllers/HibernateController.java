package ua.adeptius.asterisk.controllers;


import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.model.User;

public class HibernateController {

    public static void saveNewUser(User user) throws Exception {
        HibernateDao.saveUser(user);
        UserContainer.putUser(user);
    }

    public static void updateUser(User user) throws Exception {
        HibernateDao.update(user);
    }

    public static void removeTelephony(User user) throws Exception {
        PhonesController.releaseAllTelephonyNumbers(user.getTelephony());
        HibernateDao.removeTelephony(user);
        CallProcessor.updatePhonesHashMap();
    }
    public static void removeTracking(User user) throws Exception {
        PhonesController.releaseAllTrackingNumbers(user.getTracking());
        HibernateDao.removeTracking(user);
        CallProcessor.updatePhonesHashMap();
    }

    public static void removeUser(User user) throws Exception{
        HibernateDao.deleteUser(user.getLogin());
        PhonesController.releaseAllCustomerNumbers(user);
        UserContainer.removeUser(user);
        CallProcessor.updatePhonesHashMap();
    }

    public static void cleanServices(){
        HibernateDao.cleanServices();
    }
}