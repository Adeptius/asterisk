package ua.adeptius.asterisk;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.dao.MySqlDao;
import ua.adeptius.asterisk.model.AsteriskMonitor;
import ua.adeptius.asterisk.model.LogCategory;
import ua.adeptius.asterisk.utils.MyLogger;
import ua.adeptius.asterisk.utils.PhonesWatcher;
import ua.adeptius.asterisk.utils.Settings;
import ua.adeptius.asterisk.utils.Utils;
import java.util.List;

import static ua.adeptius.asterisk.model.LogCategory.DB_OPERATIONS;


public class Main {

    public static AsteriskMonitor monitor;
    public static MySqlDao mySqlDao;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() {
        Settings.load(this.getClass());
        mySqlDao = new MySqlDao();
        try {
            mySqlDao.init();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА  ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ ДРАЙВЕРА MYSQL");
        }

        try {
            MainController.sites = mySqlDao.getSites();
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.log(DB_OPERATIONS, "ОШИБКА ЗАГРУЗКИ КОНФИГА С БАЗЫ");
            throw new RuntimeException("ОШИБКА ЗАГРУЗКИ КОНФИГА С БАЗЫ");
        }


        new PhonesWatcher();

        try {
            mySqlDao.createOrCleanStatisticsTables();
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
//            e.printStackTrace();
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
