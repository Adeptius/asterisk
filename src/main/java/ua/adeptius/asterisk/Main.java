package ua.adeptius.asterisk;


import com.google.gson.reflect.TypeToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.Session;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.json.JsonHistoryQuery;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.newmodel.*;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
//        main.test();
//        main.test2();
    }

    private void test2() {
        try {
            String json = "[{\"from\":[\"0443211115\"],\"to\":[\"0934027182\"],\"forwardType\":\"QUEUE\",\"destinationType\":\"GSM\",\"time\":10,\"melody\":\"simple\"}]";
            List<Rule> rules = new ObjectMapper().readValue(json, new TypeReference<List<Rule>>(){});

            for (Rule rule : rules) {
                System.out.println(rule);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void test() throws Exception {

                Tracking tracking = new Tracking();
                tracking.setStandartNumber("TestNumber");
                tracking.setTimeToBlock(30);

//        List<User> users =  HibernateDao.getAllUsers();
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        List<User> list = session.createQuery("select e from User e").list();
        session.close();
        User user = list.stream().filter(user1 -> user1.getLogin().equals("newUser3")).findFirst().get();
        System.out.println("загруженный "+user);



        session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();

        session.merge(user);

//        Tracking tracking = new Tracking();
//        tracking.setUser(user);
//        user.getTracking().setUser(null);
        user.setTracking(null);



//        user.setTracking(null);
//        user.setTracking(null);
        System.out.println(user);


        session.update(user);

        session.getTransaction().commit();
        session.close();

//        HibernateDao.removeTelephony(user);


    }

    // select e from Employee e where e.name like :name


    private void init() {
        Settings.load(this.getClass());

        try {
            MySqlDao.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Загрузка обьектов
        try {
            UserContainer.setUsers(HibernateDao.getAllUsers());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ");
        }

        for (User user : UserContainer.getUsers()) {
            System.out.println(user);
        }


        try {
            MySqlStatisticDao.createOrCleanStatisticsTables();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
        }

        try {
            PhonesController.scanAndClean();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ОЧИСТКИ ЗАНЯТЫХ НОМЕРОВ В БАЗЕ");
            throw new RuntimeException("ОШИБКА ОЧИСТКИ ЗАНЯТЫХ НОМЕРОВ В БАЗЕ");
        }


        // создаём файлы конфигов номеров, если их нет
        try {
            SipConfigDao.synchronizeFilesAndDb();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Инициализация всех номеров телефонов
        UserContainer.getUsers().stream().filter(user -> user.getTracking() != null).map(User::getTracking).forEach(site -> {
            try {
                site.updateNumbers();
            } catch (Exception e) {
                e.printStackTrace();
                throw  new RuntimeException("Недостаточно номеров");
            }
        });
        UserContainer.getUsers().stream().filter(user -> user.getTelephony() != null).map(User::getTelephony).forEach(telephony -> {
            try {
                telephony.updateNumbers();
            } catch (Exception e) {
                e.printStackTrace();
                throw  new RuntimeException("Недостаточно номеров");
            }
        });

        // Загрузка правил
        UserContainer.getUsers().forEach(User::loadRules);


        CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер

        // Загрузка наблюдателя. Только для сайтов
        new PhonesWatcher();

        Thread thread = new Thread(() -> initMonitor());
        thread.setDaemon(true);
        thread.start();

        Calendar calendar = new GregorianCalendar();
        MyLogger.log(DB_OPERATIONS, "Сервер был загружен в " + calendar.get(Calendar.HOUR_OF_DAY) + " часов, " + calendar.get(Calendar.MINUTE) + " минут.");
    }

    private void initMonitor() {
        try {
            monitor = new AsteriskMonitor();
            monitor.run();
        } catch (Exception e) {
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАПУСКА МОНИТОРИНГА ТЕЛЕФОНИИ " + e.getMessage());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } finally {
            }
            MyLogger.log(DB_OPERATIONS, "ПОВТОРНО ЗАПУСКАЮ ТЕЛЕФОНИЮ");
            initMonitor();
        }
    }
}
