package com.luxoft.webapplication.controllers;


import com.luxoft.webapplication.dao.MySqlDao;
import com.luxoft.webapplication.utils.GoogleAnalitycs;
import com.luxoft.webapplication.utils.Mail;
import com.luxoft.webapplication.utils.Settings;

import java.util.GregorianCalendar;
import java.util.List;

import static com.luxoft.webapplication.utils.MyLogger.log;

public class DBController {

    private MySqlDao mySqlDao;

    public void setMySqlDao(MySqlDao mySqlDao) {
        this.mySqlDao = mySqlDao;
    }

    public void clearAllDb(){
        mySqlDao.clearAllDb();
    }

    public void setGoogleId(String phone, String googleId){
        mySqlDao.setGoogleId(phone,googleId);
    }

    public String getPhoneByGoogleId(String phone){
        return mySqlDao.getPhoneByGoogleId(phone);
    }

    public String getFreePhone(String googleId){
        if (googleId.equals("undefined")){
            log("Анонимному пользователю - стандартный номер " + Settings.standartNumber, this.getClass());
            return Settings.standartNumber;
        }
        String currentPhone = getPhoneByGoogleId(googleId);
        if (currentPhone != null && !currentPhone.equals("")){
            if (Settings.showPhoneRepeatedRequest){
                log("Повторно возвращаю пользователю его номер "+currentPhone, this.getClass());
            }
            updateTime(currentPhone);
            return currentPhone;
        }
        log("Запрос свободного номера для пользователя с googleId "+googleId, this.getClass());
        List<String> phones = mySqlDao.getFreePhones();
        StringBuilder sb = new StringBuilder();
        for (String phone : phones) {
            sb.append(phone).append(", ");
        }
        if (phones.size()>0){
            log("Есть свободный номер: " + sb.toString(), this.getClass());
            String freePhone = phones.get(0);
            log("Возвращаю свободный номер " + freePhone + " и связываю его с googleId " + googleId, this.getClass());
            setGoogleId(freePhone,googleId);
            updateTime(freePhone);
            return freePhone;
        }else {
            log("Нет свободного номера. Возвращаю стандартный.", this.getClass());
            String message = "Закончились свободные номера";
            new Mail().sendMail(message);
            return Settings.standartNumber;
        }
    }

    public String getGoogleIdByPhone(String phone){
        return mySqlDao.getGoogleIdByPhone(phone);
    }

    public void newCall(String phoneReseive, String caller){
        String googleId = getGoogleIdByPhone(phoneReseive);
        if (googleId == null || googleId.equals("")){
            log("Для номера "+phoneReseive+" нет зарегистрированного googleId", this.getClass());
        }else {
            log("К номеру "+phoneReseive+" привязан googleId "+googleId+" отправляю статистику.", this.getClass());
            new GoogleAnalitycs(googleId, caller).start();
        }
    }

    public void updateTime(String phone){
        mySqlDao.updateTime(phone);
    }

    public void removeOld(){
        mySqlDao.removeOld(new GregorianCalendar().getTimeInMillis());
    }
}
