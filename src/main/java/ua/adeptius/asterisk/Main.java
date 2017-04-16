package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.*;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.exceptions.NotEnoughNumbers;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.monitor.CallProcessor;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;
//    public static SitesDao sitesDao;
//    public static TelephonyDao telephonyDao;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() {
        Settings.load(this.getClass());

        //загрузка DAO
//        telephonyDao = new TelephonyDao();
//        try {
//            telephonyDao.init();
//        } catch (Exception e) {
//            e.printStackTrace();
//            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
//            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
//        }

//        sitesDao = new SitesDao();
//        try {
//            sitesDao.init();
//        } catch (Exception e) {
//            e.printStackTrace();
//            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL Sites");
//            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL Sites");
//        }

        try {
            MySqlDao.init();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Загрузка обьектов
        try {
            MainController.telephonyCustomers = MySqlCalltrackDao.getTelephonyCustomers();
        }catch (NotEnoughNumbers e1){
            e1.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "НЕДОСТАТОЧНО НОМЕРОВ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ТЕЛЕФОНИИ");
            throw new RuntimeException("НЕДОСТАТОЧНО НОМЕРОВ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ТЕЛЕФОНИИ");
        }catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ ТЕЛЕФОНИИ С БАЗЫ");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ ТЕЛЕФОНИИ С БАЗЫ");
        }

        try {
            MainController.sites = MySqlCalltrackDao.getSites();
        }catch (NotEnoughNumbers e1){
            e1.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "НЕДОСТАТОЧНО НОМЕРОВ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ТРЕКИНГА");
            throw new RuntimeException("НЕДОСТАТОЧНО НОМЕРОВ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ ТРЕКИНГА");
        }catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ ТРЕКИНГА С БАЗЫ");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ПОЛЬЗОВАТЕЛЕЙ ТРЕКИНГА С БАЗЫ");
        }

        // Загрузка наблюдателя. Только для сайтов
        new PhonesWatcher();

        // создание и удаление таблиц статистики
//        try {
//            sitesDao.createOrCleanStatisticsTables();
//        } catch (Exception e) {
//            e.printStackTrace();
//            MyLogger.log(DB_OPERATIONS, "ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТРЕКИНГА");
//            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТРЕКИНГА");
//        }

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

        CallProcessor.updatePhonesHashMap(); // обновляем мапу для того что бы знать с кем связан номер

        // Мониторинг телефонии
        Thread thread = new Thread(() -> initMonitor());
        thread.setDaemon(true);
        thread.start();
    }


    private void initMonitor(){
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
