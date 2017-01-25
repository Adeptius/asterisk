package com.luxoft.webapplication.dao;


import com.luxoft.webapplication.utils.MyLogger;
import com.luxoft.webapplication.utils.Settings;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.luxoft.webapplication.utils.MyLogger.log;

@SuppressWarnings("Duplicates")
public class MySqlDao {

    private ComboPooledDataSource cpds;
    public static final String TABLE = "asterisk";
    public static final String USER = "user";
    public static final String PASSWORD = "1234";
    private Object phoneByGoogleId;
    private List<Long> timeAllPhones;

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
            log("Ошибка подключения к базе...", this.getClass());
            e.printStackTrace();
            return null;
        }
    }

//    public void savePhone(String phone, String googleId) {
//        String sql = "INSERT INTO "
//                + TABLE
//                + " values('" + phone + "','false','" + googleId + "')";
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection.prepareStatement(sql)) {
//            connection.setAutoCommit(true);
//            statement.execute();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public void clearAllDb() {
        String sql = "UPDATE " + TABLE
                + " SET googleid = NULL";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(true);
            statement.execute();
        } catch (SQLException e) {
            log("Ошибка очистки базы", this.getClass());
            e.printStackTrace();
        }
    }

//    public void setPhoneIsBusy(String phone) {
//        String sql = "UPDATE " + TABLE + " SET status = 'busy' WHERE phone = '" + phone + "'";
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection.prepareStatement(sql)) {
//            connection.setAutoCommit(true);
//            statement.execute();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public List<String> getFreePhones() {
        List<String> freePhones = new ArrayList<>();
        String sql = "SELECT phone from " + TABLE + " WHERE googleid is NULL or googleid = ''";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                freePhones.add(set.getString("phone"));
//                System.out.println(set.getString("phone"));
            }
        } catch (SQLException e) {
            log("Ошибка получения свободных номеров из базы.", this.getClass());
            e.printStackTrace();
            List<String> list = new ArrayList<>();
            list.add(Settings.standartNumber);
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
            log("Ошибка связывания номера с googleId", this.getClass());
            e.printStackTrace();
        }
    }

    public String getGoogleIdByPhone(String phone) {
        String sql = "SELECT googleid FROM "+TABLE+" WHERE phone = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            ResultSet set = statement.executeQuery();
            while (set.next()){
                return set.getString("googleid");
            }
        } catch (SQLException e) {
            log("Ошибка получения googleId из базы", this.getClass());
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public String getPhoneByGoogleId(String googleid) {
        String sql = "SELECT phone FROM "+TABLE+" WHERE googleid = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, googleid);
            ResultSet set = statement.executeQuery();
            while (set.next()){
                return set.getString("phone");
            }
            return null;
        } catch (SQLException e) {
            log("Ошибка получения телефона по googleId. Возвращаю стандартный", this.getClass());
            e.printStackTrace();
            return Settings.standartNumber;
        }
    }

    public void setPhoneIsFree(String phone) {
        String sql = "UPDATE "+TABLE+" SET googleid = NULL, time = NULL WHERE phone = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Ошибка очистки номера", this.getClass());
            e.printStackTrace();
        }
    }

    public void updateTime(String phone) {
        String sql = "UPDATE "+TABLE+" SET time_left = ? WHERE phone = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, new GregorianCalendar().getTimeInMillis()
                    + (Settings.phoneTimeToRemoveInSeconds * 1000));
            statement.setString(2, phone);
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Ошибка обновления времени для телефона " + phone, this.getClass());
            e.printStackTrace();
        }
    }

    public void removeOld(long timeNow) {
        String sql = "UPDATE "+TABLE+" SET googleid = NULL, time_left = NULL WHERE time_left < ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, timeNow);
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Ошибка проверки времени", this.getClass());
            MyLogger.printException(e);
        }
    }
}
