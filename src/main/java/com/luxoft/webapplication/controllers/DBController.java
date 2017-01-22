package com.luxoft.webapplication.controllers;


import com.luxoft.webapplication.dao.PostgreDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DBController {


    private PostgreDao postgreDao;

    public void setPostgreDao(PostgreDao postgreDao) {
        this.postgreDao = postgreDao;
    }

    @Transactional
    public void savePhone(String phone){
        postgreDao.savePhone(phone);
    }
}
