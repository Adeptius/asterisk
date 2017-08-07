package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class MySqlDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(MySqlDao.class.getSimpleName());


    private static ComboPooledDataSource statisticDataSource;
    private static ComboPooledDataSource trackingDataSource;

    public static void init() throws Exception {
        String login = Settings.getSetting("dbLogin");
        String password = Settings.getSetting("dbPassword");
        statisticDataSource = new ComboPooledDataSource();
        statisticDataSource.setDriverClass("com.mysql.jdbc.Driver");
        statisticDataSource.setJdbcUrl("jdbc:mysql://" + Settings.getSetting("dbAdress") + "statisticdb");
        statisticDataSource.setUser(login);
        statisticDataSource.setPassword(password);
        statisticDataSource.setMinPoolSize(1);
        statisticDataSource.setMaxPoolSize(5);
        statisticDataSource.setAcquireIncrement(0);


        trackingDataSource = new ComboPooledDataSource();
        trackingDataSource.setDriverClass("com.mysql.jdbc.Driver");
        trackingDataSource.setJdbcUrl("jdbc:mysql://" + Settings.getSetting("dbAdress") + "calltrackdb");
        trackingDataSource.setUser(login);
        trackingDataSource.setPassword(password);
        trackingDataSource.setMinPoolSize(1);
        trackingDataSource.setMaxPoolSize(5);
        trackingDataSource.setAcquireIncrement(0);
    }

    protected static Connection getStatisticConnection() throws Exception {
            return statisticDataSource.getConnection();
    }

    protected static Connection getTrackingConnection() throws Exception {
            return trackingDataSource.getConnection();
    }
}
