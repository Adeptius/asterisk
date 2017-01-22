package com.luxoft.webapplication.dao;


import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

public class PostgreDao {

    @Autowired
    private DataSource dataSource;


}
