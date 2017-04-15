package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;

import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.monitor.Call;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_ERROR_CONNECTING;
import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;
import static ua.adeptius.asterisk.utils.logging.MyLogger.printException;

@SuppressWarnings("Duplicates")
public class SitesDao {

    private ComboPooledDataSource cpds;
    public static final String SITE_TABLE = "sites";
    public static final String DB_URL =  "jdbc:mysql://" + Settings.getSetting("___dbAdress") + "calltrackdb";

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
        String sql = "SELECT * from "+SITE_TABLE;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                sites.add(new Site(
                        set.getString("name"),
//                        DaoHelper.getListFromString(set.getString("phones")).stream().map(Phone::new).collect(Collectors.toList()),
                        set.getString("standart_number"),
                        set.getString("tracking_id"),
                        set.getString("email"),
                        DaoHelper.getListFromString(set.getString("black_list_ip")),
                        set.getString("password"),
                        set.getInt("time_to_block"),
                        set.getInt("outer_phones")
                ));
            }
            return sites;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при загрузке сайтов с БД");
        throw new Exception("Ошибка при загрузке сайтов с БД");
    }


    public boolean deleteSite(String name) throws Exception {
        String sql = DaoHelper.createSqlQueryForDeleteSite(name);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при удалении сайтов с БД");
        throw new Exception("Ошибка при удалении сайтов с БД");
    }


    public boolean saveSite(Site site) throws Exception {
        String sql = DaoHelper.createSqlQueryForSaveSite(site);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, site.getName() + " сохранён");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка сохранения сайта в БД: " + e.getMessage());
            throw new Exception("Ошибка сохранения сайта в БД: " + e.getMessage());
        }
    }

    public boolean editSite(Site site) throws Exception {
        Connection connection = getConnection();
        String sqlDelete = DaoHelper.createSqlQueryForDeleteSite(site.getName());
        String sqlSave = DaoHelper.createSqlQueryForSaveSite(site);
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
            log(DB_OPERATIONS, "Ошибка изменения сайта в базе" + site.getName());
            throw new Exception("Ошибка изменения сайта " + site.getName() + " в базу: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public List<String> getListOfTables() throws Exception {
        String sql = "show tables like 'statistic_%'";
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
        }
        log(DB_OPERATIONS, "Ошибка при поиске таблиц статистики с БД");
        throw new Exception("Ошибка при загрузке таблиц статистики с БД");
    }


    public List<Statistic> getStatisticOfRange(String sitename, String startDate, String endDate, String direction) throws Exception {
        String sql = "SELECT * FROM calltrackdb.statistic_" +
                sitename +
                " WHERE direction = '"+direction+
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


    public void deleteTables(List<String> tablesToDelete) {
        for (String s : tablesToDelete) {
            String sql = "DROP TABLE `"+ s +"`";
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
                log(DB_OPERATIONS, "Таблица " + s + " была удалена.");
            } catch (Exception e) {
                e.printStackTrace();
                log(DB_OPERATIONS, "Ошибка удаления ненужной таблицы с бд " + s);
            }
        }
    }


    public void createStatisticTables(List<String> tablesToCreate) {
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
            }
        }
    }

    public void saveCall(Call call, String googleId, String request) {
        String sql = "INSERT INTO `statistic_"+call.getCustomer().getName()+"` VALUES ('"
                +call.getCalled()+"', '"
                +call.getDirection()+"', '"
                +call.getFrom()+"', '"
                +call.getTo()+"', '"
                +call.getCallState()+"', '"
                +call.getAnswered()+"', '"
                +call.getEnded()+"', '"
                +call.getId()+"', '"
                +googleId+"', '"
                +request+"');";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, call.getCustomer().getName() + ": Ошибка при сохранении отчета в БД ");
        }
    }

    public void createOrCleanStatisticsTables() throws Exception{
        List<String> tables = getListOfTables();
        List<String> tablesToDelete = DaoHelper.findTablesThatNeedToDeleteSite(MainController.sites, tables);
        deleteTables(tablesToDelete);
        List<String> tablesToCreate = DaoHelper.findTablesThatNeedToCreateSite(MainController.sites, tables);
        createStatisticTables(tablesToCreate);
    }

    public void addIpToBlackList(String siteName, String ip) throws Exception {
        Site site = MainController.getSiteByName(siteName);
        site.getBlackIps().add(ip);
        String s = getBlackList(siteName);
        s += "," + ip;
        if (s.length() > 1500){
            s = s.substring(s.indexOf(","));
            s = s.substring(s.indexOf(","));
        }
        setBlackList(siteName, s);
    }

    public String deleteFromBlackList(String SiteName, String ip) throws Exception {
        String s = getBlackList(SiteName);
        if (s.contains(","+ip)){
            s = s.replaceAll(","+ip, "");
            setBlackList(SiteName, s);
            Site site = MainController.getSiteByName(SiteName);
            site.getBlackIps().remove(ip);
            return "Success: IP " + ip + " unblocked.";
        }else if (s.contains(ip)){
            s = s.replaceAll(ip, "");
            setBlackList(SiteName, s);
            Site site = MainController.getSiteByName(SiteName);
            site.getBlackIps().remove(ip);
            return "Success: IP " + ip + " unblocked.";
        }
        return "Error: IP " + ip + " not blocked.";
    }

    private void setBlackList(String sitename, String ip) throws Exception {
        String sql = "UPDATE `"+SITE_TABLE+"` SET `black_list_ip`='"+ip+"' WHERE `name`='"+sitename+"';";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, sitename + ": Ошибка при сохранении черного списка в БД ");
            throw new Exception(sitename + ": Ошибка при сохранении черного списка в БД ");
        }
    }

    private String getBlackList(String sitename) throws Exception{
        String sql = "SELECT `black_list_ip` FROM `"+SITE_TABLE+"` WHERE `name` like \""+sitename+"\"";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            if (set.next()){
                return set.getString("black_list_ip");
            }
            throw new RuntimeException();
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, sitename + ": Ошибка при загрузке черного списка из БД ");
            throw new Exception(sitename + ": Ошибка при загрузке черного списка из БД ");
        }
    }


    public void setTimeToBlock(String name, int time) throws Exception{
        String sql = "UPDATE `"+SITE_TABLE+"` SET `time_to_block`='"+time+"' WHERE `name`='"+name+"';";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            MainController.getSiteByName(name).setTimeToBlock(time);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, name + ": Ошибка при установке времени блокировки в БД");
            throw new Exception(name + ": Ошибка при установке времени блокировки в БД");
        }
    }
}
