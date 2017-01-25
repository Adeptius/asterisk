package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.utils.Settings;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static ua.adeptius.asterisk.utils.MyLogger.log;
import static ua.adeptius.asterisk.utils.MyLogger.printException;


public class MySqlDao {

    private ComboPooledDataSource cpds;
    public static String TABLE = Settings.dbTableName;
    public static String PHONE = Settings.dbColumnPhoneName;
    public static String GOOGLEID = Settings.dbColumnGoogleIdName;
    public static String TIMELEFT = Settings.dbColumnTimeToDieName;

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");
        cpds.setJdbcUrl("jdbc:mysql://"+Settings.dbAdress);
        cpds.setUser(Settings.dbLogin);
        cpds.setPassword(Settings.dbPassword);
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(5);
        cpds.setAcquireIncrement(0);
    }

    private Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            log("Ошибка подключения к базе...", this.getClass());
            printException(e);
            return null;
        }
    }

    public void clearAllDb() {
        String sql = "UPDATE " + TABLE + " SET "+GOOGLEID+" = NULL";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(true);
            statement.execute();
        } catch (SQLException e) {
            log("Ошибка очистки базы", this.getClass());
            printException(e);
        }
    }

    public List<String> getFreePhones() {
        List<String> freePhones = new ArrayList<>();
        String sql = "SELECT "+PHONE+" from "+TABLE+" WHERE "+GOOGLEID+" is NULL or "+GOOGLEID+" = ''";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                freePhones.add(set.getString("phone"));
            }
        } catch (SQLException e) {
            log("Ошибка получения свободных номеров из базы.", this.getClass());
            printException(e);
            List<String> list = new ArrayList<>();
            list.add(Settings.standartNumber);
        }
        return freePhones;
    }

    public void setGoogleId(String phone, String googleId) {
        String sql = "UPDATE "+TABLE+" SET "+GOOGLEID+" = ? WHERE "+PHONE+" = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, googleId);
            statement.setString(2, phone);
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Ошибка связывания номера с googleId", this.getClass());
            printException(e);
        }
    }

    public String getGoogleIdByPhone(String phone) {
        String sql = "SELECT "+GOOGLEID+" FROM "+TABLE+" WHERE "+PHONE+" = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, phone);
            ResultSet set = statement.executeQuery();
            while (set.next()){
                return set.getString("googleid");
            }
        } catch (SQLException e) {
            log("Ошибка получения googleId из базы", this.getClass());
            printException(e);
        }
        throw new RuntimeException();
    }

    public String getPhoneByGoogleId(String googleid) {
        String sql = "SELECT "+PHONE+" FROM "+TABLE+" WHERE "+GOOGLEID+" = ?";
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
            printException(e);
            return Settings.standartNumber;
        }
    }

    public void updateTime(String phone) {
        String sql = "UPDATE "+TABLE+" SET "+TIMELEFT+" = ? WHERE "+PHONE+" = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, new GregorianCalendar().getTimeInMillis()
                    + (Settings.phoneTimeToRemoveInSeconds * 1000));
            statement.setString(2, phone);
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Ошибка обновления времени для телефона " + phone, this.getClass());
            printException(e);
        }
    }

    public void removeOld(long timeNow) {
        String sql = "UPDATE "+TABLE+" SET "+GOOGLEID+" = NULL, "+TIMELEFT+" = NULL WHERE "+TIMELEFT+" < ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, timeNow);
            statement.executeUpdate();
        } catch (SQLException e) {
            log("Ошибка проверки времени", this.getClass());
            printException(e);
        }
    }
}
