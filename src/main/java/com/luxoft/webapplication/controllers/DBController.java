package com.luxoft.webapplication.controllers;


import com.luxoft.webapplication.dao.MySqlDao;
import com.luxoft.webapplication.utils.GoogleAnalitycs;
import com.luxoft.webapplication.utils.Mail;

import java.util.List;

import static com.luxoft.webapplication.utils.MyLogger.log;

public class DBController {

    private MySqlDao mySqlDao;

    public void setMySqlDao(MySqlDao mySqlDao) {
        this.mySqlDao = mySqlDao;
    }

    public void setPhoneIsBusy(String phone) {
        mySqlDao.setPhoneIsBusy(phone);
    }

    public void setGoogleId(String phone, String googleId){
        mySqlDao.setGoogleId(phone,googleId);
    }

    public String getPhoneByGoogleId(String phone){
        return mySqlDao.getPhoneByGoogleId(phone);
    }

    public String getFreePhone(String googleId){
        log("Запрос свободного номера для пользователя с googleId "+googleId, this.getClass());
        String currentPhone = getPhoneByGoogleId(googleId);
        if (currentPhone != null && !currentPhone.equals("")){
            log("Пользователю уже был выдан номер "+currentPhone, this.getClass());
            return currentPhone;
        }
        List<String> phones = mySqlDao.getFreePhones();
        StringBuilder sb = new StringBuilder();
        for (String phone : phones) {
            sb.append(phone).append(", ");
        }
        if (phones.size()>0){
            log("Есть свободный номер: " + sb.toString(), this.getClass());
            String freePhone = phones.get(0);
            setPhoneIsBusy(freePhone);
            log("Возвращаю свободный номер " + freePhone + " и связываю его с googleId " + googleId, this.getClass());
            setGoogleId(freePhone,googleId);
            return freePhone;
        }else {
            log("Нет свободного номера. Возвращаю стандартный.", this.getClass());
            String message = "Закончились свободные номера";
            new Mail().sendMail(message);
            return"5555555";
        }
    }

    public String getGoogleIdByPhone(String phone){
        return mySqlDao.getGoogleIdByPhone(phone);
    }

    public void newCall(String phoneReseive){
        String googleId = getGoogleIdByPhone(phoneReseive);
        if (googleId == null || googleId.equals("")){
            log("Для номера "+phoneReseive+" нет зарегистрированного googleId", this.getClass());
        }else {
            log("К номеру "+phoneReseive+" привязан googleId "+googleId+" отправляю статистику.", this.getClass());
            new GoogleAnalitycs(googleId, phoneReseive).start();
        }
    }
}
