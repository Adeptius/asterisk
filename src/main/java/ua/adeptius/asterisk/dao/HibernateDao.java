package ua.adeptius.asterisk.dao;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.SipConfig;


import java.util.List;

public class HibernateDao {


    private static Logger LOGGER = LoggerFactory.getLogger(HibernateDao.class.getSimpleName());

    public static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

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
//    public static List<Scenario> getAllScenarios() throws Exception {
//        Session session = sessionFactory.openSession();
//        List<Scenario> list = session.createQuery("select s from Scenario s").list();
//        session.close();
//        return list;
//    }

    //    Ерунда какая-то. Если вставить в модель данные отсюда - хибернейт ломается при следующем сохранении user'a
    // Решено - надо было пересоздать таблицу. Заглючил mysql
    public static List<Scenario> getAllScenariosByUser(User user) {
        Session session = sessionFactory.openSession();
        List<Scenario> list = session.createQuery("FROM Scenario S where S.login = '" + user.getLogin() + "'").list();
        list.forEach(scenario -> scenario.setUser(user));
        session.close();
        return list;
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

    /**
     * Telephony
     */

    public static Telephony getTelephonyByUser(User user) {
        Session session = sessionFactory.openSession();
        String hql = "FROM Telephony T WHERE T.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", user.getLogin());
        Telephony telephony = (Telephony) query.uniqueResult();
        if (telephony != null) {
            telephony.setUser(user);
        }
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
        if (tracking != null) {
            tracking.setUser(user);
        }
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
        if (amoAccount != null) {
            amoAccount.setUser(user);
        }
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
        if (roistatAccount != null) {
            roistatAccount.setUser(user);
        }
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


    /**
     * Inner phones
     */
    public static List<InnerPhone> getAllInnerPhones() throws Exception {
        LOGGER.info("Загрузка внутренних номеров");
        Session session = sessionFactory.openSession();
        List<InnerPhone> list = session.createQuery("select i from InnerPhone i").list();
        session.close();
        return list;
    }

    public static List<InnerPhone> getAllInnerUserPhones(String user) throws Exception {
        LOGGER.info("Загрузка внутренних номеров привязаных к пользователю {}", user);
        Session session = sessionFactory.openSession();
        List<InnerPhone> list = session.createQuery("select i from InnerPhone i where i.busy = '" + user + "'").list();
        session.close();
        return list;
    }

    public static void saveSipBySipConfig(SipConfig sipConfig, String user) throws Exception {
        LOGGER.debug("{}: Сохраняю SIP конфиг в БД: {}",user, sipConfig.getNumber());

        InnerPhone innerPhone = new InnerPhone();
        innerPhone.setNumber(sipConfig.getNumber());
        innerPhone.setPass(sipConfig.getPassword());
        innerPhone.setBusy(user);

        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(innerPhone);

        session.getTransaction().commit();
        session.close();
    }

    public static int getSipMaxNumber() throws Exception {
        LOGGER.trace("Поиск в базе максимального номера телефона");
        Session session = sessionFactory.openSession();
        InnerPhone innerPhone = (InnerPhone) session
                .createQuery("from InnerPhone i where i.number = (select max(ii.number) from InnerPhone ii)")
                .uniqueResult();
        String number = innerPhone.getNumber();
        return Integer.parseInt(number);
    }

    public static void markInnerPhonesBusy(String user, List<String> numbers) throws Exception {
        LOGGER.trace("{}: помечаем {} номеров занятыми {}", user, numbers.size(), numbers);
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (String number : numbers) {
            session.load(InnerPhone.class, number).setBusy(user);
        }

        session.getTransaction().commit();
        session.close();
    }

    public static void removeInnerPhone(List<String> numbersToRelease) throws Exception {
        LOGGER.trace("Удаляем {} внутренних номеров {}", numbersToRelease.size(), numbersToRelease);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        for (String number : numbersToRelease) {
            InnerPhone phone = session.load(InnerPhone.class, number);
            session.delete(phone);
        }
        session.getTransaction().commit();
        session.close();
    }


    /**
     * Outer Phones
     */

    public static List<OuterPhone> getAllOuterUsersPhones(String user) throws Exception {
        LOGGER.info("{}: загрузка внешних номеров пользователя", user);
        Session session = sessionFactory.openSession();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.busy = '" + user + "'").list();
        session.close();
        return list;
    }


    public static List<OuterPhone> getAllFreeOuterPhones() throws Exception {
        LOGGER.info("Загрузка свободных внешних телефонов");
        Session session = sessionFactory.openSession();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.busy = null").list();
        session.close();
        return list;
    }

    public static List<OuterPhone> getAllBusyOuterPhones() throws Exception {
        LOGGER.info("Загрузка занятых внешних телефонов");
        Session session = sessionFactory.openSession();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.busy != null").list();
        session.close();
        return list;
    }


    public static void markOuterPhoneBusy(String user, List<String> numbers) throws Exception {
        LOGGER.trace("{}: помечаем {} внешних номеров занятыми {}", user, numbers.size(), numbers);
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (String number : numbers) {
            session.load(OuterPhone.class, number).setBusy(user);
        }

        session.getTransaction().commit();
        session.close();
    }

    public static void markOuterPhoneFree(List<String> numbersToRelease) throws Exception {
        LOGGER.trace("Освобождаем {} внешних номеров {}", numbersToRelease.size(), numbersToRelease);
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (String s : numbersToRelease) {
            OuterPhone phone = session.load(OuterPhone.class, s);
            phone.setBusy(null);
        }

        session.getTransaction().commit();
        session.close();
    }
}