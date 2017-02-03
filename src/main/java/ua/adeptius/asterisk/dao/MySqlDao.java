package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.PreparedStatement;

import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.PhoneStatistic;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.MyLogger;
import ua.adeptius.asterisk.utils.Settings;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.model.LogCategory.DB_ERROR_CONNECTING;
import static ua.adeptius.asterisk.model.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.MyLogger.log;
import static ua.adeptius.asterisk.utils.MyLogger.printException;


public class MySqlDao {

    private ComboPooledDataSource cpds;
    public static String TABLE = Settings.getSetting("___dbTableName");
//    public static String PHONE = Settings.dbColumnPhoneName;
//    public static String GOOGLEID = Settings.dbColumnGoogleIdName;
//    public static String TIMELEFT = Settings.dbColumnTimeToDieName;

    public void init() throws Exception {
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.mysql.jdbc.Driver");
        cpds.setJdbcUrl("jdbc:mysql://" + Settings.getSetting("___dbAdress"));
        cpds.setUser(Settings.getSetting("___dbLogin"));
        cpds.setPassword(Settings.getSetting("___dbPassword"));
        cpds.setMinPoolSize(1);
        cpds.setMaxPoolSize(2);
        cpds.setAcquireIncrement(0);
    }

    private Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            log(DB_ERROR_CONNECTING, "Ошибка подключения к базе...");
            printException(e);
            return null;
        }
    }


    public List<Site> getSites() throws Exception {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT * from " + TABLE;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {

                // парсим телефоны
                String[] p = set.getString("phones").split(",");
                List<Phone> phones = new ArrayList<>();
                int start = 0;
                if (p[0].equals("")) {
                    start = 1;
                }
                for (int i = start; i < p.length; i++) {
                    phones.add(new Phone(p[i]));
                }

                // парсим черный список
                List<String> ips = new ArrayList<>();
                String s = set.getString("black_list_ip");
                if (s != null) {
                    String[] ip = s.split(",");
                    start = 0;
                    if (ip[0].equals("")) {
                        start = 1;
                    }
                    for (int i = start; i < ip.length; i++) {
                        ips.add(ip[i]);
                    }
                }

                sites.add(new Site(
                        set.getString("name"),
                        set.getString("adress"),
                        phones,
                        set.getString("standart_number"),
                        set.getString("tracking_id"),
                        set.getString("email"),
                        ips
                ));
            }
            return sites;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при загрузке данных с БД");
        throw new Exception("Ошибка при загрузке данных с БД");
    }


    public boolean deleteSite(String name) throws Exception {
        String sql = createSqlQueryForDeleteSite(name);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при удалении данных с БД");
        throw new Exception("Ошибка при удалении данных с БД");
    }


    public boolean saveSite(Site site) throws Exception {
        String sql = createSqlQueryForSaveSite(site);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, site.getName() + " сохранён");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка сохранения данных в БД");
        throw new Exception("Ошибка сохранения данных в БД");
    }

    public boolean editSite(Site site) throws Exception {
        Connection connection = getConnection();

        String sqlDelete = createSqlQueryForDeleteSite(site.getName());
        String sqlSave = createSqlQueryForSaveSite(site);

        try (Statement deleteStatement = connection.createStatement();
             Statement addStatement = connection.createStatement()) {

            connection.setAutoCommit(false);

            deleteStatement.execute(sqlDelete);
            addStatement.execute(sqlSave);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                connection.rollback();
            }
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
        log(DB_OPERATIONS, "Ошибка изменения сайта " + site.getName());
        throw new Exception("Ошибка изменения сайта " + site.getName());
    }


    public String createSqlQueryForCtreatingStatisticTable(String name) {
        String sql = "CREATE TABLE `" + name + "` (  " +
                "`date` VARCHAR(20) NOT NULL,  " +
                "`to` VARCHAR(45) NULL,  " +
                "`from` VARCHAR(45) NULL,  " +
                "`time_to_answer` INT NULL,  " +
                "`talking_time` INT NULL,  " +
                "PRIMARY KEY (`date`));";
        return sql;
    }

    public String createSqlQueryForDeleteSite(String site) {
        return "DELETE from " + TABLE + " WHERE name = '" + site + "'";
    }

    public String createSqlQueryForSaveSite(Site site) {
        String name = site.getName();
        String url = site.getAccessControlAllowOrigin();
        String email = site.getMail();
        String standartNumber = site.getStandartNumber();
        String googleId = site.getGoogleAnalyticsTrackingId();
        String phones = "";
        String blackList = "";
        List<Phone> phoneList = site.getPhones();
        for (Phone phone : phoneList) {
            phones += "," + phone.getNumber();
        }
        if (phones.startsWith(",")) {
            phones = phones.substring(1);
        }

        List<String> blackIPList = site.getBlackIps();
        for (String s : blackIPList) {
            blackList += "," + s;
        }
        if (blackList.startsWith(",")) {
            blackList = blackList.substring(1);
        }

        String sql = "INSERT INTO " + TABLE + " VALUES("
                + "'" + name + "',"
                + "'" + url + "',"
                + "'" + googleId + "',"
                + "'" + email + "',"
                + "'" + phones + "',"
                + "'" + standartNumber + "',"
                + "'" + blackList + "')";

        return sql;
    }

    public List<String> getListOfTables() throws Exception {
        String sql = "show tables like 'statistic_%'";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            List<String> listOfTables = new ArrayList<>();
            String columnName = Settings.getSetting("___dbAdress");
            columnName = columnName.substring(columnName.lastIndexOf("/") + 1);
            columnName = "Tables_in_" + columnName + " (statistic_%)";
            while (set.next()) {
                listOfTables.add(set.getString(columnName));
            }
            return listOfTables;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при загрузке данных с БД");

        throw new Exception("Ошибка при загрузке данных с БД");
    }


    public void deleteTables(List<String> tablesToDelete) {
        for (String s : tablesToDelete) {
            String sql = "DROP TABLE " + s;
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                log(DB_OPERATIONS, "Таблица " + s + " была удалена.");
            } catch (Exception e) {
                e.printStackTrace();
                log(DB_OPERATIONS, "ОШИБКА УДАЛЕНИЯ НЕНУЖНОЙ ТАБЛИЦЫ С БД " + s);
            }
        }
    }


    public void createStatisticTables(List<String> tablesToCreate) {
        for (String s : tablesToCreate) {
            s = "statistic_" + s;
            String sql = createSqlQueryForCtreatingStatisticTable(s);
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                log(DB_OPERATIONS, "Таблица " + s + " была создана.");
            } catch (Exception e) {
                e.printStackTrace();
                log(DB_OPERATIONS, "Ошибка при создании таблицы в БД " + s);
            }
        }
    }


    public void saveStatisticToTable(Site site, PhoneStatistic statistic) {
        String insertTableSQL = "INSERT INTO ? VALUES(?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertTableSQL)) {
            statement.setString(1, "statistic_" + site.getName());
            statement.setString(2, statistic.getDateForDb());
            statement.setString(3, statistic.getTo());
            statement.setString(4, statistic.getFrom());
            statement.setInt(5, statistic.getTimeToAnswerInSeconds());
            statement.setInt(6, statistic.getSpeakTimeInSeconds());
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, site.getName() + ": Ошибка при сохранении отчета в БД ");
        }
    }
}
