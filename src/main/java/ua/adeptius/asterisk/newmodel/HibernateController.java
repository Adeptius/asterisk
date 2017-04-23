package ua.adeptius.asterisk.newmodel;


import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.monitor.CallProcessor;

public class HibernateController {

    public static void saveNewUser(User user) throws Exception {
        HibernateDao.saveUser(user);
        UserContainer.putUser(user);
    }

    public static void addTracking(User user, Tracking tracking) throws Exception {
        tracking.setUser(user);
        tracking.updateNumbers();
        user.setTracking(tracking);
        HibernateDao.update(user);
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
        UserContainer.getUsers().remove(user);
        CallProcessor.updatePhonesHashMap();
    }
}