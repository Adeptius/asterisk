package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.TelephonyDao;
import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.SitesDao;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;
import ua.adeptius.asterisk.dao.Settings;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;
    public static SitesDao sitesDao;
    public static TelephonyDao telephonyDao;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() {
        Settings.load(this.getClass());

        //загрузка DAO
        telephonyDao = new TelephonyDao();
        try {
            telephonyDao.init();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
        }

        sitesDao = new SitesDao();
        try {
            sitesDao.init();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL Sites");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL Sites");
        }

        // Загрузка обьектов
        try {
            MainController.telephonyCustomers = telephonyDao.getTelephonyCustomers();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MainController.sites = sitesDao.getSites();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАГРУЗКИ КОНФИГА С БАЗЫ");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ КОНФИГА С БАЗЫ");
        }

        // Раздаём номера
//        try {
//            PhonesController.giveOutNumbers();
//        }catch (Exception e){
//            e.printStackTrace();
//            MyLogger.log(DB_OPERATIONS, "ОШИБКА ВЫДАЧИ НОМЕРОВ");
//            throw new RuntimeException("ОШИБКА ВЫДАЧИ НОМЕРОВ");
//        }

        // Загрузка наблюдателя. Только для сайтов
        new PhonesWatcher();

        // создание и удаление таблиц статистики
        try {
            sitesDao.createOrCleanStatisticsTables();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТРЕКИНГА");
            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТРЕКИНГА");
        }

        try {
            telephonyDao.createOrCleanStatisticsTables();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ ТЕЛЕФОНИИ");
        }

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
