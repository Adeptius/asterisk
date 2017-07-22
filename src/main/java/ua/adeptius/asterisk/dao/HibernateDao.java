package ua.adeptius.asterisk.dao;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.*;


import java.util.List;

public class HibernateDao {


    private static Logger LOGGER =  LoggerFactory.getLogger(HibernateDao.class.getSimpleName());

    private static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

    /**
     * User
     */

    public static List<User> getAllUsers() throws Exception {
        Session session = sessionFactory.openSession();
        List<User> list = session.createQuery("select e from User e").list();
        session.close();
        return list;
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

    public static User getUserByLogin(String login) throws Exception {
        Session session = sessionFactory.openSession();
        User user = session.get(User.class, login);
        session.close();
        return user;
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
     * Scenario
     */

    public static List<Scenario> getAllScenarios() throws Exception {
        Session session = sessionFactory.openSession();
        List<Scenario> list = session.createQuery("select s from Scenario s").list();
        session.close();
        return list;
    }

//    Ерунда какая-то. Если вставить в модель данные отсюда - хибернейт ломается при следующем сохранении user'a
//    public static List<Scenario> getAllScenariosByUser(User user) {
//        Session session = sessionFactory.openSession();
//        String hql = "FROM Scenario S WHERE S.login = :login";
//        Query query = session.createQuery(hql);
//        query.setParameter("login", user.getLogin());
//        List<Scenario> list = query.list();
//        session.close();
//        return list;
//    }

    public static Scenario getScenarioById(int id) throws Exception {
        Session session = sessionFactory.openSession();
        Scenario scenario = session.get(Scenario.class, id);
        session.close();
        return scenario;
    }

    public static void update(Scenario scenario) {
        LOGGER.info("Обновление сценария {}", scenario.getId());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.update(scenario);

        session.getTransaction().commit();
        session.close();
    }

    public static void remove(Scenario scenario) {
        LOGGER.info("Удаление сценария из БД: {}", scenario.toString());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.remove(scenario);
        scenario.getUser().getScenarios().remove(scenario);

        session.getTransaction().commit();
        session.close();
    }

    public static void saveScenario(Scenario scenario) throws Exception {
        LOGGER.info("Сохранение сценария {}", scenario.getId());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(scenario);

        session.getTransaction().commit();
        session.close();
    }

    /**
     * Telephony
     */

    public static Telephony getTelephonyByUser(User user) {
        Session session = sessionFactory.openSession();
        String hql = "FROM Telephony T WHERE T.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", user.getLogin());
        Telephony telephony = (Telephony) query.uniqueResult();
        session.close();
        return telephony;
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


    /**
     * Tracking
     */

    public static Tracking getTrackingByUser(User user) {
        Session session = sessionFactory.openSession();
        String hql = "FROM Tracking T WHERE T.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", user.getLogin());
        Tracking tracking = (Tracking) query.uniqueResult();
        session.close();
        return tracking;
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

    /**
     * AmoCRM
     */
    public static AmoAccount getAmoAccountByUser(User user) {
        Session session = sessionFactory.openSession();
        String hql = "FROM AmoAccount A WHERE A.nextelLogin = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", user.getLogin());
        AmoAccount amoAccount = (AmoAccount) query.uniqueResult();
        session.close();
        return amoAccount;
    }

    public static void removeAmoAccount(User user) {
        LOGGER.info("Удаление amo аккаунта у пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(user.getAmoAccount());
        user.setAmoAccount(null);
        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    /**
     * Roistat
     */
    public static RoistatAccount getRoistatAccountByUser(User user) {
        Session session = sessionFactory.openSession();
        String hql = "FROM RoistatAccount R WHERE R.nextelLogin = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", user.getLogin());
        RoistatAccount roistatAccount = (RoistatAccount) query.uniqueResult();
        session.close();
        return roistatAccount;
    }

    public static void removeRoistatAccount(User user) {
        LOGGER.info("Удаление roistat аккаунта у пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(user.getRoistatAccount());
        user.setRoistatAccount(null);
        session.update(user);

        session.getTransaction().commit();
        session.close();
    }
}
