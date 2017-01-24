package com.luxoft.webapplication;


import com.luxoft.webapplication.controllers.DBController;
import com.luxoft.webapplication.dao.MySqlDao;
import com.luxoft.webapplication.model.AsteriskMonitor;
import com.luxoft.webapplication.utils.Mail;
import com.luxoft.webapplication.utils.MyLogger;
import com.luxoft.webapplication.utils.Utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.luxoft.webapplication.utils.MyLogger.*;

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
//        dbController.clearAllDb();


        String googleId = Utils.getFakeGoogleId();
        String free = dbController.getFreePhone(googleId);


    }
}
