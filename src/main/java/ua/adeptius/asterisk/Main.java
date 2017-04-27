package ua.adeptius.asterisk;



import ua.adeptius.asterisk.controllers.HibernateController;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.controllers.UserContainer;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.monitor.DailyCleaner;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }


    private void init() {
        Settings.load(this.getClass());

        try {
            MySqlDao.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Чистка сервисов, если пользователь по какой-то причине удалён, а сервис остался
        try {
            HibernateController.cleanServices();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ОШИБКА УДАЛЕНИЯ УСЛУГ ВЛАДЕЛЬЦЕВ КОТОРЫХ БОЛЬШЕ НЕТ");
        }


//        Загрузка обьектов
        try {
            UserContainer.setUsers(HibernateDao.getAllUsers());
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ");
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
        new DailyCleaner();

        Thread thread = new Thread(() -> initMonitor());
        thread.setDaemon(true);
        thread.start();

        // Чистим правила всех пользователей
        for (User user : UserContainer.getUsers()) {
            System.out.println(user);
            try {
                RulesConfigDAO.removeFileIfNeeded(user);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

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
