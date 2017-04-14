package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.model.TelephonyCustomer;
import ua.adeptius.asterisk.controllers.MainController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_ERROR_CONNECTING;
import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;
import static ua.adeptius.asterisk.utils.logging.MyLogger.logAndThrow;
import static ua.adeptius.asterisk.utils.logging.MyLogger.printException;

@SuppressWarnings("Duplicates")
public class TelephonyDao {

    private static ComboPooledDataSource cpds;
    public static final String TELEPHONY_TABLE = "telephony_users";
    public static final String DB_URL = "jdbc:mysql://" + Settings.getSetting("___dbAdress") + "telephonydb";

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");
        cpds.setJdbcUrl(DB_URL);
        cpds.setUser(Settings.getSetting("___dbLogin"));
        cpds.setPassword(Settings.getSetting("___dbPassword"));
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(2);
        cpds.setAcquireIncrement(0);
    }

    public static Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            log(DB_ERROR_CONNECTING, "Ошибка подключения к базе...");
            printException(e);
            return null;
        }
    }

    public static List<String> getMelodies() throws Exception {
        List<String> melodies = new ArrayList<>();
        String sql = "SELECT * FROM `melodies`";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
               melodies.add(set.getString("name"));
            }
            return melodies;
        } catch (Exception e) {
            e.printStackTrace();
        }
        logAndThrow(DB_OPERATIONS, "Ошибка при загрузке пользователей телефонии с БД");
        throw new Exception("Ошибка при загрузке пользователей телефонии с БД");
    }



    public List<TelephonyCustomer> getTelephonyCustomers() throws Exception {
        List<TelephonyCustomer> telephonyCustomers = new ArrayList<>();
        String sql = "SELECT * FROM " + TELEPHONY_TABLE;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                telephonyCustomers.add(new TelephonyCustomer(
                        set.getString("name"),
                        set.getString("email"),
                        set.getString("tracking_id"),
                        set.getString("password"),
                        set.getInt("inner_phones"),
                        set.getInt("outer_phones")
                ));
            }
            return telephonyCustomers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        logAndThrow(DB_OPERATIONS, "Ошибка при загрузке пользователей телефонии с БД");
        throw new Exception("Ошибка при загрузке пользователей телефонии с БД");
    }


    public void saveTelephonyCustomer(TelephonyCustomer newCustomer) throws Exception {
        String sql = DaoHelper.getQueryForSaveTelephonyCustomer(newCustomer);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, newCustomer.getName() + " сохранён");
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка сохранения пользователя в БД: " + e);
            throw new Exception("Ошибка сохранения пользователя в БД: " + e);
        }
    }

    public void editTelephonyCustomer(TelephonyCustomer newCustomer) throws Exception {
        String sql = DaoHelper.getQueryForEditTelephonyCustomer(newCustomer);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, newCustomer.getName() + " сохранён");
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка изменения пользователя в БД: " + e);
            throw new Exception("Ошибка изменения пользователя в БД: " + e);
        }
    }

    public void deleteTelephonyCustomer(String name) throws Exception {
        String sql = DaoHelper.createSqlQueryForDeleteTelephonyCustomer(name);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при удалении пользователя с БД");
            throw new Exception("Ошибка при удалении пользователя с БД");
        }
    }

    public void createOrCleanStatisticsTables() throws Exception {
        List<String> tables = getListOfTables();
        List<String> tablesToDelete = DaoHelper.findTablesThatNeedToDeleteTelephony(MainController.telephonyCustomers, tables);
        deleteTables(tablesToDelete);
        List<String> tablesToCreate = DaoHelper.findTablesThatNeedToCreateTelephony(MainController.telephonyCustomers, tables);
        createStatisticTables(tablesToCreate);

    }

    public List<String> getListOfTables() throws Exception {
        String sql = "SHOW TABLES LIKE 'statistic_%'";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<String> listOfTables = new ArrayList<>();
            String columnName = DB_URL;
            columnName = columnName.substring(columnName.lastIndexOf("/") + 1);
            columnName = "Tables_in_" + columnName + " (statistic_%)";
            while (set.next()) {
                listOfTables.add(set.getString(columnName));
            }
            return listOfTables;
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при поиске таблиц статистики с БД");
            throw new Exception("Ошибка при загрузке таблиц статистики с БД");
        }
    }

    public void deleteTables(List<String> tablesToDelete) throws Exception {
        for (String s : tablesToDelete) {
            String sql = "DROP TABLE `" + s + "`";
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                log(DB_OPERATIONS, "Таблица " + s + " была удалена.");
            } catch (Exception e) {
                e.printStackTrace();
                log(DB_OPERATIONS, "Ошибка удаления ненужной таблицы с бд " + s);
                throw new Exception("Ошибка удаления ненужной таблицы с бд " + s);
            }
        }
    }

    public void createStatisticTables(List<String> tablesToCreate) throws Exception {
        for (String s : tablesToCreate) {
            s = "statistic_" + s;
            String sql = DaoHelper.createSqlQueryForCtreatingStatisticTable(s);
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                log(DB_OPERATIONS, "Таблица " + s + " была создана.");
            } catch (Exception e) {
                e.printStackTrace();
                log(DB_OPERATIONS, "Ошибка при создании таблицы в БД " + s);
                throw new Exception("Ошибка при создании таблицы в БД " + s);
            }
        }
    }

    public List<Statistic> getStatisticOfRange(String sitename, String startDate, String endDate, String direction) throws Exception {
        String sql = "SELECT * FROM telephonydb.statistic_" +
                sitename +
                " WHERE direction = '" + direction +
                "' AND date BETWEEN STR_TO_DATE('" +
                startDate +
                "', '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE('" +
                endDate +
                "', '%Y-%m-%d %H:%i:%s')";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<Statistic> statisticList = new ArrayList<>();
            while (set.next()) {
                Statistic statistic = new Statistic();
                statistic.setDate(set.getString("date"));
                statistic.setDirection(set.getString("direction"));
                statistic.setTo(set.getString("to"));
                statistic.setFrom(set.getString("from"));
                statistic.setTimeToAnswer(set.getInt("time_to_answer"));
                statistic.setTalkingTime(set.getInt("talking_time"));
                statistic.setGoogleId(set.getString("google_id"));
                statistic.setCallUniqueId(set.getString("call_id"));
                statistic.setRequestWithoutFiltering(set.getString("utm"));
                statisticList.add(statistic);
            }
            return statisticList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при загрузке данных с БД");
        throw new Exception("Ошибка при загрузке данных с БД");
    }
}
