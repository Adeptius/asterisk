package com.luxoft.webapplication.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class MySqlDao {

    private ComboPooledDataSource cpds;
    public static final String TABLE = "asterisk";
    public static final String USER = "user";
    public static final String PASSWORD = "1234";

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        // com.mysql.jdbc.Driver
        // org.postgresql.Driver
        cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
        // jdbc:mysql://localhost/test
        cpds.setJdbcUrl("jdbc:mysql://localhost:3306/sys");
        cpds.setUser(USER);
        cpds.setPassword(PASSWORD);
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(5);
        cpds.setAcquireIncrement(0);
    }

    private Connection getConnection() {
        try {
//            return connectionPool.getConnection();
            return cpds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePhone(String phone, String googleId) {
        String sql = "INSERT INTO "
                + TABLE
                + " values('" + phone + "','false','" + googleId + "')";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(true);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void clearAllDb() {
        String sql = "UPDATE " + TABLE
                + " SET status = 'free', googleid = NULL";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(true);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setPhoneIsBusy(String phone) {
        String sql = "UPDATE " + TABLE
                + " SET status = 'busy' WHERE phone = '" + phone + "'";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(true);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<String> getFreePhones() {
        List<String> freePhones = new ArrayList<>();
        String sql = "SELECT phone from " + TABLE + " WHERE status = 'free'";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                freePhones.add(set.getString("phone"));
//                System.out.println(set.getString("phone"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return freePhones;
    }

    public void setGoogleId(String phone, String googleId) {
        String sql = "UPDATE "+TABLE+" SET googleid = ? WHERE phone = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, googleId);
            statement.setString(2, phone);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
