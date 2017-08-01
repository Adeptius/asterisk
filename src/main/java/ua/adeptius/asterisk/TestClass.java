package ua.adeptius.asterisk;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.HibernateDao;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.model.*;
import ua.adeptius.asterisk.telephony.ForwardType;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static ua.adeptius.asterisk.telephony.DestinationType.SIP;

public class TestClass {

    public static void main(String[] args) throws Exception {
//        SipConfig sipConfig = new SipConfig("111","351");
//        PhonesDao.saveSipToDB(sipConfig);

//        int maxNumber = PhonesDao.getMaxSipNumber();
//        System.out.println(maxNumber);

//        HashMap<String, String> sipPasswords = PhonesDao.getSipPasswords("e404");
//        sipPasswords.forEach((s, s2) -> {
//            System.out.println(s+"="+s2);
//        });

//        HashMap<String, String> sipPasswords = PhonesDao.getAllSipsAndPass();
//        sipPasswords.forEach((s, s2) -> {
//            System.out.println(s + "=" + s2);
//        });

//        ArrayList<String> customersNumbers = PhonesDao.getCustomersNumbers("trac_e404", false);
//        customersNumbers.forEach(System.out::println);

//        ArrayList<String> customersNumbers = PhonesDao.getCustomersNumbers("promtek", true);
//        customersNumbers.forEach(System.out::println);

//        HashMap<String, String> outerPhones = PhonesDao.getBusyOuterPhones();
//        outerPhones.forEach((s, s2) -> {
//            System.out.println(s+"="+s2);
//        });

//        HashMap<String, String> busyInnerPhones = PhonesDao.getBusyInnerPhones();
//        busyInnerPhones.forEach((s, s2) -> {
//            System.out.println(s+"="+s2);
//        });

//        ArrayList<String> freePhones = PhonesDao.getFreePhones(false);
//        freePhones.forEach(s -> System.out.println(s));

//        List<String> numbersToMakeBusy = new ArrayList<>();
//        numbersToMakeBusy.add("0443211127");
//        numbersToMakeBusy.add("0443211128");
//        PhonesDao.markNumbersBusy(numbersToMakeBusy, "justForTest", false);


//        List<String> numbersToMakeFree = new ArrayList<>();
//        numbersToMakeFree.add("0443211127");
//        numbersToMakeFree.add("0443211128");
//        PhonesDao.markNumberFree(numbersToMakeFree, false);


//        List<String> numbersToDelete = new ArrayList<>();
//        numbersToDelete.add("111");
//        PhonesDao.deleteNumbersFromDb(numbersToDelete, true);

//        long t0 = System.nanoTime();
//        List<String> list = new ArrayList<>();
//        list.add("0443211127");
//        list.add("0443211128");
//        list.add("0443211129");
//        list.add("0443211131");
//        list.add("0443211134");
//        list.add("0443211135");
//        HibernateDao.markOuterPhoneFree(list);
//        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0));


//        HibernateDao.justForTest();




//        Session session = HibernateDao.sessionFactory.openSession();
//        session.beginTransaction();
//        Telephony telephony = session.get(Telephony.class, "e404");
//
//        session.getTransaction().commit();
//        session.close();
//
//        session = HibernateDao.sessionFactory.openSession();
//        session.beginTransaction();
//
//        telephony = (Telephony) session.merge(telephony);
////        telephony.setOuterCount();
//
//        session.getTransaction().commit();
//        session.close();


//        User user = HibernateDao.getUserByLogin("e404");
//        AmoAccount amoAccount = user.getAmoAccount();
//        amoAccount.addBinding("111", "222");
//        HibernateDao.update(user);

        System.out.println(new Date().getTime());


    }
}
