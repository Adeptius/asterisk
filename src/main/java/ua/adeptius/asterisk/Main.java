package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.newmodel.*;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
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
        MainController.users = HibernateDao.getAllUsers();

        for (User user : MainController.users) {
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
        MainController.users.stream().filter(user -> user.getSite() != null).map(User::getSite).forEach(site -> {
            try {
                site.updateNumbers();
            } catch (Exception e) {
                e.printStackTrace(); // TODO перехватить ошибку недостаточно номеров
            }
        });
        MainController.users.stream().filter(user -> user.getTelephony() != null).map(User::getTelephony).forEach(telephony -> {
            try {
                telephony.updateNumbers();
            } catch (Exception e) {
                e.printStackTrace(); // TODO перехватить ошибку недостаточно номеров
            }
        });

        // Загрузка правил
        MainController.users.forEach(User::loadRules);


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
