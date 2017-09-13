package ua.adeptius.asterisk.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.util.ArrayList;
import java.util.List;

@Component
public class HibernateController {

    private static Logger LOGGER = LoggerFactory.getLogger(HibernateController.class.getSimpleName());

    private static HibernateDao hibernateDao;






    @Autowired
    public void setHibernateDao(HibernateDao hibernateDao) {
        HibernateController.hibernateDao = hibernateDao;
    }


    public static int operationsCount;
    public static List<Long> time = new ArrayList<>();

    /**
     * User
     */

    public static List<User> getAllUsers() throws Exception {
        return hibernateDao.getAllUsers();
    }

    public static void saveUser(User user) throws Exception {
        hibernateDao.saveUser(user);
    }

    public static void delete(User user) {
        hibernateDao.delete(user);
    }


    public static void update(User user) {
        hibernateDao.update(user);
    }

    public static User getUserByLogin(String login) throws Exception {
//        operationsCount++;
//        long t0 = System.nanoTime();
        User userByLogin = hibernateDao.getUserByLogin(login);
//        time.add(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0));
        return userByLogin;
    }

    /**
     * PendingUser
     */
    public static void saveOrUpdate(RegisterQuery registerQuery) {
        LOGGER.info("{}: Сохранение запроса на регистрацию...", registerQuery.getLogin());
        hibernateDao.saveOrUpdate(registerQuery);
    }

    public static void removeRegisterQueryByLogin(String login){
        hibernateDao.removeRegisterQueryByLogin(login);
    }

    public static void removeRegisterQuery(RegisterQuery registerQuery){
        hibernateDao.removeRegisterQuery(registerQuery);
    }

    public static RegisterQuery getRegisterQueryByKey(String key){
        return hibernateDao.getRegisterQueryByHash(key);
    }

    public static List<RegisterQuery> getAllRegisterQueries() {
        return hibernateDao.getAllRegisterQueries();
    }


    /**
     * Recover user
     */
    public static void saveOrUpdate(RecoverQuery recoverQuery) {
       hibernateDao.saveOrUpdate(recoverQuery);
    }

    public static RecoverQuery getRecoverQueryByHash(String hash){
       return hibernateDao.getRecoverQueryByHash(hash);
    }

    public static void removeRecoverQuery(RecoverQuery recoverQuery){
        hibernateDao.removeRecoverQuery(recoverQuery);
    }

    public static List<RecoverQuery> getAllRecoverQueries(){
       return hibernateDao.getAllRecoverQueries();
    }

    public static void removeRecoverQueryByLogin(String name){
        hibernateDao.removeRecoverQueryByLogin(name);
    }

    /**
     * Melodies
     */

    public static List<String> getMelodies() {
        return hibernateDao.getMelodies();
    }


    /**
     * AmoCRM
     */
    // Для тестов
    public static AmoAccount getAmoAccountByUser(String nextelLogin) {
        return hibernateDao.getAmoAccountByUser(nextelLogin);
    }


    /**
     * Roistat
     */
    // Для тестов
    public static RoistatAccount getRoistatAccountByUser(String nextelLogin) {
        return hibernateDao.getRoistatAccountByUser(nextelLogin);
    }


    /**
     * Operator location
     */
    // Для тестов
    public static AmoOperatorLocation getAmoOperatorLocationByUser(String login) {
        return hibernateDao.getAmoOperatorLocationByUser(login);
    }


    /**
     * Inner phones
     */

    public static List<InnerPhone> getAllInnerPhones() throws Exception {
        return hibernateDao.getAllInnerPhones();
    }

    @Deprecated
    public static List<InnerPhone> getAllInnerUserPhones(String user) throws Exception {
        return hibernateDao.getAllInnerUserPhones(user);
    }

    @Deprecated
    public static InnerPhone saveSipBySipConfig(SipConfig sipConfig, String user) throws Exception {
        return hibernateDao.saveSipBySipConfig(sipConfig, user);
    }


    public static int getSipMaxNumber() throws Exception {
        return hibernateDao.getSipMaxNumber();
    }


    /**
     * Outer Phones
     */

    public static List<OuterPhone> getAllFreeOuterPhones() throws Exception {
        return hibernateDao.getAllFreeOuterPhones();
    }

    @Deprecated
    public static List<OuterPhone> getAllBusyOuterPhones() throws Exception {
        return hibernateDao.getAllBusyOuterPhones();
    }

    public static List<OuterPhone> getOuterPhonesWithScenario() throws Exception {
        return hibernateDao.getOuterPhonesWithScenario();
    }

    public static void markOuterPhoneBusy(String user, List<String> numbers) throws Exception {
        hibernateDao.markOuterPhoneBusy(user, numbers);
    }

    public static void markOuterPhoneFree(List<String> numbersToRelease) throws Exception {
        LOGGER.trace("Освобождаем {} внешних номеров {}", numbersToRelease.size(), numbersToRelease);

        hibernateDao.markOuterPhoneFree(numbersToRelease);
    }


    // Используется в тестах
    public static List<OuterPhone> getAllTestPhones() throws Exception {
        return hibernateDao.getAllTestPhones();
    }

    public static void removeAllTestPhones() throws Exception {
        hibernateDao.removeAllTestPhones();
    }

    public static void createTestPhonesForUser(String user, String siteName) throws Exception {
        hibernateDao.createTestPhonesForUser(user, siteName);
    }

    public static void delete(Object o) {
        hibernateDao.delete(o);
    }

    public static List<Rule> getAllRules(){
       return hibernateDao.getAllRules();
    }
}