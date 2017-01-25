package com.luxoft.webapplication;


import com.luxoft.webapplication.controllers.DBController;
import com.luxoft.webapplication.dao.MySqlDao;
import com.luxoft.webapplication.model.AsteriskMonitor;
import com.luxoft.webapplication.utils.Cleaner;


public class Main {

    public static AsteriskMonitor monitor;
    public static MySqlDao mySqlDao;
    public static DBController dbController;

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.init();
    }

    private void init() throws Exception {
        mySqlDao = new MySqlDao();
        mySqlDao.init();
        dbController = new DBController();
        dbController.setMySqlDao(mySqlDao);

        monitor = new AsteriskMonitor();
        monitor.setDbController(dbController);
        monitor.run();
        dbController.clearAllDb();

        new Cleaner(dbController);
    }
}
