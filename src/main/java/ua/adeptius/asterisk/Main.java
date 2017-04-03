package ua.adeptius.asterisk;


import ua.adeptius.asterisk.dao.TelephonyDao;
import ua.adeptius.asterisk.model.TelephonyCustomer;
import ua.adeptius.asterisk.tracking.MainController;
import ua.adeptius.asterisk.dao.SitesDao;
import ua.adeptius.asterisk.monitor.AsteriskMonitor;
import ua.adeptius.asterisk.utils.logging.MyLogger;
import ua.adeptius.asterisk.monitor.PhonesWatcher;
import ua.adeptius.asterisk.dao.Settings;

import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;
    public static SitesDao sitesDao;
    public static TelephonyDao telephonyDao;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
//        main.telephonyInit();

    }

    private void telephonyInit() {
        Settings.load(this.getClass());



    }

    private void init() {
        Settings.load(this.getClass());

        telephonyDao = new TelephonyDao();
        try {
            telephonyDao.init();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
        }

        try {
            MainController.telephonyCustomers = telephonyDao.getTelephonyCustomers();
//            for (TelephonyCustomer telephonyCustomer : MainController.telephonyCustomers) {
//                System.out.println(telephonyCustomer);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sitesDao = new SitesDao();
        try {
            sitesDao.init();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL Sites");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL Sites");
        }

        try {
            MainController.sites = sitesDao.getSites();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАГРУЗКИ КОНФИГА С БАЗЫ");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ КОНФИГА С БАЗЫ");
        }

        new PhonesWatcher();

        try {
            sitesDao.createOrCleanStatisticsTables();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ");
            throw new RuntimeException("ОШИБКА СОЗДАНИЯ ИДИ УДАЛЕНИЯ ТАБЛИЦ СТАТИСТИКИ В БАЗЕ");
        }

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
