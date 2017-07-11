package ua.adeptius.asterisk.dao;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ua.adeptius.asterisk.model.AmoAccount;
import ua.adeptius.asterisk.model.Telephony;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.model.User;

import java.util.List;

public class HibernateDao {


    private static Logger LOGGER =  LoggerFactory.getLogger(HibernateDao.class.getSimpleName());

    private static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

    public static List<User> getAllUsers() throws Exception {
        Session session = sessionFactory.openSession();
        List<User> list = session.createQuery("select e from User e").list();
        session.close();
        return list;
    }

    public static User getUserByLogin(String login) throws Exception {
        Session session = sessionFactory.openSession();
        User user = session.get(User.class, login);
        session.close();
        return user;
    }


    public static void saveUser(User user) throws Exception {
        LOGGER.info("Сохранение пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(user);

        session.getTransaction().commit();
        session.close();
    }


    public static void update(User user) {
        LOGGER.info("Обновление пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public static void removeTelephony(User user) {
        LOGGER.info("Удаление телефонии у пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(user.getTelephony());
        user.setTelephony(null);
        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public static void removeTracking(User user) {
        LOGGER.info("Удаление трекинга у пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(user.getTracking());
        user.setTracking(null);
        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public static void deleteUser(String username) {
        LOGGER.info("Удаление телефонии у пользователя {}", username);
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        User user = session.get(User.class, username);
        user.setTelephony(null);
        user.setTracking(null);
        session.delete(user);

        session.getTransaction().commit();
        session.close();
    }


    /**
     * Неактуально - наложены внешние ключи в БД
     */
    public static void cleanServices() {
        LOGGER.debug("Удаление услуг, которые никому не принадлежат");
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        List<Tracking> trackings = session.createQuery("select t from Tracking t").list();
        List<Telephony> telephonies = session.createQuery("select t from Telephony t").list();
        List<AmoAccount> amoAccounts = session.createQuery("select a from AmoAccount a").list();

        for (Telephony telephony : telephonies) {
            if (telephony.getUser() == null){
                session.delete(telephony);
            }
        }

        for (Tracking tracking : trackings) {
            if (tracking.getUser() == null){
                session.delete(tracking);
            }
        }

        for (AmoAccount amoAccount : amoAccounts) {
            if (amoAccount.getUser() == null){
                session.delete(amoAccount);
            }
        }

        session.getTransaction().commit();
        session.close();
    }
}
