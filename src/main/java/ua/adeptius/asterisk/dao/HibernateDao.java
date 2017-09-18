package ua.adeptius.asterisk.dao;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.model.telephony.*;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class HibernateDao {


    private static Logger LOGGER = LoggerFactory.getLogger(HibernateDao.class.getSimpleName());

//    private static SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();

    private static SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        HibernateDao.sessionFactory = sessionFactory;
    }

    /**
     * User
     */

    public List<User> getAllUsers() throws Exception {
        Session session = sessionFactory.openSession();
        List<User> list = session.createQuery("select e from User e").list();
        session.close();
        return list;
    }

    public void saveUser(User user) throws Exception {
        LOGGER.info("Сохранение пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(user);

        session.getTransaction().commit();
        session.close();
    }

    public void update(User user) {
        LOGGER.info("{}: Обновление пользователя...", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.update(user);

        session.getTransaction().commit();
        session.close();
    }

    public User getUserByLogin(String login) throws Exception {
        Session session = sessionFactory.openSession();
        User user = session.get(User.class, login);
        session.close();
        return user;
    }

    public User getUserByEmail(String email){
        Session session = sessionFactory.openSession();
        String hql = "FROM User U WHERE U.email = :email";
        Query query = session.createQuery(hql);
        query.setParameter("email", email);
        User user = (User) query.uniqueResult();
        session.close();
        return user;
    }


    public void delete(User user) {
        LOGGER.info("Удаление пользователя {}", user.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        Set<OuterPhone> outerPhones = user.getOuterPhones();
        for (OuterPhone outerPhone : outerPhones) {
            outerPhone.setBusy(null);
            outerPhone.setSitename(null);
            outerPhone.setScenarioId(null);
            session.update(outerPhone);
        }

        session.getTransaction().commit();
        session.beginTransaction();

        user = session.get(User.class, user.getLogin());
        session.delete(user);
        session.getTransaction().commit();
        session.close();
    }


    /**
     * PendingUser
     */
    public void saveOrUpdate(RegisterQuery registerQuery) {
        LOGGER.info("{}: Обновление ожидающего пользователя...", registerQuery.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.saveOrUpdate(registerQuery);

        session.getTransaction().commit();
        session.close();
    }

    public RegisterQuery getRegisterQueryByHash(String hash){
        Session session = sessionFactory.openSession();
        String hql = "FROM RegisterQuery P WHERE P.hash = :hash";
        Query query = session.createQuery(hql);
        query.setParameter("hash", hash);
        RegisterQuery registerQuery = (RegisterQuery) query.uniqueResult();
        session.close();
        return registerQuery;
    }

    public void removeRegisterQueryByLogin(String login){
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        String hql = "FROM RegisterQuery P WHERE P.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", login);
        RegisterQuery registerQuery = (RegisterQuery) query.uniqueResult();
        session.delete(registerQuery);
        session.getTransaction().commit();
        session.close();
    }

    public void removeRegisterQuery(RegisterQuery registerQuery){
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(registerQuery);

        session.getTransaction().commit();
        session.close();
    }

    public List<RegisterQuery> getAllRegisterQueries(){
        Session session = sessionFactory.openSession();
        List<RegisterQuery> list = session.createQuery("select p from RegisterQuery p").list();
        session.close();
        return list;
    }


    /**
     * RecoverUser
     */
    public void saveOrUpdate(RecoverQuery recoverQuery) {
        LOGGER.info("{}: Обновление пользователя для восстановления пароля...", recoverQuery.getLogin());
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.saveOrUpdate(recoverQuery);

        session.getTransaction().commit();
        session.close();
    }

    public RecoverQuery getRecoverQueryByHash(String hash){
        Session session = sessionFactory.openSession();
        String hql = "FROM RecoverQuery R WHERE R.hash = :hash";
        Query query = session.createQuery(hql);
        query.setParameter("hash", hash);
        RecoverQuery recoverQuery = (RecoverQuery) query.uniqueResult();
        session.close();
        return recoverQuery;
    }

    public void removeRecoverQuery(RecoverQuery recoverQuery){
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(recoverQuery);

        session.getTransaction().commit();
        session.close();
    }

    public void removeRecoverQueryByLogin(String name){
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        String hql = "DELETE FROM RecoverQuery R WHERE R.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", name);
        query.executeUpdate();

        session.getTransaction().commit();
        session.close();
    }

    public List<RecoverQuery> getAllRecoverQueries(){
        Session session = sessionFactory.openSession();
        List<RecoverQuery> list = session.createQuery("select r from RecoverQuery r").list();
        session.close();
        return list;
    }




//    public static void deleteUser(String username) {
//        LOGGER.info("Удаление телефонии у пользователя {}", username);
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        User user = session.get(User.class, username);
//        user.setTelephony(null);
//        user.setTracking(null);
//        session.delete(user);
//
//        session.getTransaction().commit();
//        session.close();
//    }


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
//    @Deprecated
//    public static List<Scenario> getAllScenariosByUser(User user) {
//        Session session = sessionFactory.openSession();
//        List<Scenario> list = session.createQuery("FROM Scenario S where S.login = '" + user.getLogin() + "'").list();
//        list.forEach(scenario -> scenario.setUser(user));
//        session.close();
//        return list;
//    }

//    public static void remove(Scenario scenario) {
//        LOGGER.info("Удаление сценария из БД: {}", scenario.toString());
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        session.remove(scenario);
//        scenario.getUser().getScenarios().remove(scenario);
//
//        session.getTransaction().commit();
//        session.close();
//    }

    /**
     * Rules
     */


    public List<Rule> getRuleByUser(String login) throws Exception {
        Session session = sessionFactory.openSession();
        String hql = "FROM Rule R WHERE R.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", login);
        List<Rule> list = query.list();
        session.close();
        return list;
    }


    public List<ChainElement> getChainsByUser(String login) throws Exception {
        Session session = sessionFactory.openSession();
        String hql = "FROM ChainElement C WHERE C.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", login);
        List<ChainElement> list = query.list();
        session.close();
        return list;
    }



    /**
     * Melodies
     */

    public List<String> getMelodies() {
        Session session = sessionFactory.openSession();
         List<Melody> melodies = session.createQuery("FROM Melody M").list();
        session.close();
        return melodies.stream().map(Melody::getName).collect(Collectors.toList());
    }

    /**
     * Tracking
     */

//    public static Tracking getTrackingByUser(User user) {
//        Session session = sessionFactory.openSession();
//        String hql = "FROM Tracking T WHERE T.login = :login";
//        Query query = session.createQuery(hql);
//        query.setParameter("login", user.getLogin());
//        Tracking tracking = (Tracking) query.uniqueResult();
//        if (tracking != null) {
//            tracking.setUser(user);
//        }
//        session.close();
//        return tracking;
//    }

//    public static void removeTracking(User user) {
//        LOGGER.info("Удаление трекинга у пользователя {}", user.getLogin());
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        session.delete(user.getTracking());
//        user.setTracking(null);
//        session.update(user);
//
//        session.getTransaction().commit();
//        session.close();
//    }

    /**
     * AmoCRM
     */
    // Для тестов
    public AmoAccount getAmoAccountByUser(String nextelLogin) {
        Session session = sessionFactory.openSession();
        String hql = "FROM AmoAccount A WHERE A.nextelLogin = :nextelLogin";
        Query query = session.createQuery(hql);
        query.setParameter("nextelLogin", nextelLogin);
        AmoAccount amoAccount = (AmoAccount) query.uniqueResult();
        session.close();
        return amoAccount;
    }

//    @Deprecated
//    public static void removeAmoAccount(User user) {
//        LOGGER.info("Удаление amo аккаунта у пользователя {}", user.getLogin());
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        session.delete(user.getAmoAccount());
//        user.setAmoAccount(null);
//        session.update(user);
//
//        session.getTransaction().commit();
//        session.close();
//    }

    /**
     * Roistat
     */
    // Для тестов
    public RoistatAccount getRoistatAccountByUser(String nextelLogin) {
        Session session = sessionFactory.openSession();
        String hql = "FROM RoistatAccount R WHERE R.nextelLogin = :nextelLogin";
        Query query = session.createQuery(hql);
        query.setParameter("nextelLogin", nextelLogin);
        RoistatAccount roistatAccount = (RoistatAccount) query.uniqueResult();
        session.close();
        return roistatAccount;
    }


    /**
     * Operator location
     */
    // Для тестов
    public AmoOperatorLocation getAmoOperatorLocationByUser(String login) {
        Session session = sessionFactory.openSession();
        String hql = "FROM AmoOperatorLocation A WHERE A.login = :login";
        Query query = session.createQuery(hql);
        query.setParameter("login", login);
        AmoOperatorLocation operatorLocation = (AmoOperatorLocation) query.uniqueResult();
        session.close();
        return operatorLocation;
    }

//    @Deprecated
//    public static void removeRoistatAccount(User user) {
//        LOGGER.info("Удаление roistat аккаунта у пользователя {}", user.getLogin());
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        session.delete(user.getRoistatAccount());
//        user.setRoistatAccount(null);
//        session.update(user);
//
//        session.getTransaction().commit();
//        session.close();
//    }


    /**
     * Inner phones
     */

    public List<InnerPhone> getAllInnerPhones() throws Exception {
        LOGGER.info("Загрузка внутренних номеров");
        Session session = sessionFactory.openSession();
        List<InnerPhone> list = session.createQuery("select i from InnerPhone i").list();
        session.close();
        return list;
    }

    @Deprecated
    public List<InnerPhone> getAllInnerUserPhones(String user) throws Exception {
        LOGGER.info("Загрузка внутренних номеров привязаных к пользователю {}", user);
        Session session = sessionFactory.openSession();
        List<InnerPhone> list = session.createQuery("select i from InnerPhone i where i.busy = '" + user + "'").list();
        session.close();
        return list;
    }

    @Deprecated
    public InnerPhone saveSipBySipConfig(SipConfig sipConfig, String user) throws Exception {
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
        return innerPhone;
    }


    public int getSipMaxNumber() throws Exception {
        LOGGER.trace("Поиск в базе максимального номера телефона");
        Session session = sessionFactory.openSession();
        InnerPhone innerPhone = (InnerPhone) session
                .createQuery("from InnerPhone i where i.number = (select max(ii.number) from InnerPhone ii)")
                .uniqueResult();
        String number = innerPhone.getNumber();
        session.close();
        return Integer.parseInt(number);
    }

//    @Deprecated
//    public static void markInnerPhonesBusy(String user, List<String> numbers) throws Exception {
//        LOGGER.trace("{}: помечаем {} номеров занятыми {}", user, numbers.size(), numbers);
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//
//        for (String number : numbers) {
//            session.load(InnerPhone.class, number).setBusy(user);
//        }
//
//        session.getTransaction().commit();
//        session.close();
//    }

//    @Deprecated
//    public static void removeInnerPhone(List<String> numbersToRelease) throws Exception {
//        LOGGER.trace("Удаляем {} внутренних номеров {}", numbersToRelease.size(), numbersToRelease);
//        Session session = sessionFactory.openSession();
//        session.beginTransaction();
//        for (String number : numbersToRelease) {
//            InnerPhone phone = session.load(InnerPhone.class, number);
//            session.delete(phone);
//        }
//        session.getTransaction().commit();
//        session.close();
//    }


    /**
     * Outer Phones
     */

//    @Deprecated
//    public static List<OuterPhone> getAllOuterUsersPhones(String user) throws Exception {
//        LOGGER.info("{}: загрузка внешних номеров пользователя", user);
//        Session session = sessionFactory.openSession();
//        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.busy = '" + user + "'").list();
//        session.close();
//        return list;
//    }


    public List<OuterPhone> getAllFreeOuterPhones() throws Exception {
        LOGGER.info("Загрузка свободных внешних телефонов");
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.busy = null").list();
        for (OuterPhone outerPhone : list) {
            if (outerPhone.getSitename() != null){
                outerPhone.setSitename(null);
            }
        }
        session.getTransaction().commit();
        session.close();
        return list;
    }

    @Deprecated
    public List<OuterPhone> getAllBusyOuterPhones() throws Exception {
        LOGGER.info("Загрузка занятых внешних телефонов");
        Session session = sessionFactory.openSession();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.busy != null").list();
        session.close();
        return list;
    }

    public List<OuterPhone> getOuterPhonesWithScenario() throws Exception {
        LOGGER.info("Загрузка внешних телефонов со сценариями");
        Session session = sessionFactory.openSession();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.scenarioId != null").list();
        session.close();
        return list;
    }


    @Deprecated
    public void markOuterPhoneBusy(String user, List<String> numbers) throws Exception {
        LOGGER.trace("{}: помечаем {} внешних номеров занятыми {}", user, numbers.size(), numbers);
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (String number : numbers) {
            session.load(OuterPhone.class, number).setBusy(user);
        }

        session.getTransaction().commit();
        session.close();
    }


    public void markOuterPhoneFree(Collection<String> numbersToRelease) throws Exception {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (String s : numbersToRelease) {
            OuterPhone phone = session.load(OuterPhone.class, s);
            phone.setBusy(null);
            phone.setSitename(null);
            phone.setScenarioId(null);
        }

        session.getTransaction().commit();
        session.close();
    }


    // Используется в тестах
    public List<OuterPhone> getAllTestPhones() throws Exception {
        LOGGER.info("Загрузка внешних тестовых номеров");
        Session session = sessionFactory.openSession();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.number like 'testNumber%'").list();
        session.close();
        return list;
    }

    public void removeAllTestPhones() throws Exception {
        LOGGER.info("Удаление внешних тестовых номеров");
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        List<OuterPhone> list = session.createQuery("select o from OuterPhone o where o.number like 'testNumber%'").list();
        for (OuterPhone outerPhone : list) {
            session.delete(outerPhone);
        }
        session.getTransaction().commit();
        session.close();
    }

    public void createTestPhonesForUser(String user, String siteName) throws Exception {
        LOGGER.info("Создание внешних тестовых номеров");
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        for (int i = 1; i < 4; i++) {
            OuterPhone outerPhone = new OuterPhone();
            outerPhone.setNumber("testNumber"+i);
            outerPhone.setBusy(user);
            if (i != 3) {// создаём 2, привязываем 3
                outerPhone.setSitename(siteName);
            }
            session.save(outerPhone);
        }

        session.getTransaction().commit();
        session.close();
    }

    public void delete(Object o) {
        if (o instanceof User){
            throw new RuntimeException("Нельзя удалять пользователя этим методом.");
        }
        LOGGER.info("Удаление обьекта {}", o);
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.delete(o);

        session.getTransaction().commit();
        session.close();
    }

    public List<Rule> getAllRules(){
        LOGGER.info("Запрос всех правил");
        Session session = sessionFactory.openSession();
        List<Rule> rules = session.createQuery("FROM Rule ").list();
        session.close();
        return rules;
    }
}