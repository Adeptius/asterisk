package com.luxoft.webapplication.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.postgresql.jdbc2.optional.ConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgreDao {

    private DataSource dataSource;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreDao.class);

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void savePhone(String phone){
        String sql = "INSERT INTO free_phones values('" + phone + "','false')";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
            connection.setAutoCommit(true);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Error connecting to DB: ", e);
            e.printStackTrace();
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
