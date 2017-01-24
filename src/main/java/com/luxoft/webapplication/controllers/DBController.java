package com.luxoft.webapplication.controllers;


import com.luxoft.webapplication.dao.MySqlDao;
import com.luxoft.webapplication.utils.Mail;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.luxoft.webapplication.utils.MyLogger.log;

public class DBController {


    private MySqlDao mySqlDao;

    public void setMySqlDao(MySqlDao mySqlDao) {
        this.mySqlDao = mySqlDao;
    }

    @Transactional
    public void clearAllDb(){
        mySqlDao.clearAllDb();
    }


    @Transactional
    public void savePhone(String phone){
        mySqlDao.savePhone(phone,"");
    }

    public List<String> getFreePhones() {
        return mySqlDao.getFreePhones();
    }

    public void setPhoneIsBusy(String phone) {
        mySqlDao.setPhoneIsBusy(phone);
    }

    public void setGoogleId(String phone, String googleId){
        mySqlDao.setGoogleId(phone,googleId);
    }


    public String getFreePhone(String googleId){
        log("Запрос свободного номера...", this.getClass());
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

}
