package com.luxoft.webapplication;


import com.luxoft.webapplication.controllers.DBController;
import com.luxoft.webapplication.dao.MySqlDao;
import com.luxoft.webapplication.model.AsteriskMonitor;
import com.luxoft.webapplication.utils.GoogleAnalitycs;
import com.luxoft.webapplication.utils.Utils;

import static com.luxoft.webapplication.utils.MyLogger.*;

// TODO сделать настройки в файле
// TODO что если пользователь уйдёт с сайта раньше, чем позвонит?

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
//        monitor.run();

//        System.out.println(dbController.getPhoneByGoogleId("5535147155.8234668455"));


//        dbController.clearAllDb();

//        String googleId = Utils.getFakeGoogleId();
//        String free = dbController.getFreePhone(googleId);

//        String googleId = dbController.getGoogleIdByPhone("85678");
//        System.out.println(googleId);
//        new GoogleAnalitycs(googleId).start();


    }
}
