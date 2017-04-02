package ua.adeptius.asterisk.dao;


import com.mchange.v2.c3p0.ComboPooledDataSource;

import ua.adeptius.asterisk.tracking.TrackingController;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.model.Statistic;
import ua.adeptius.asterisk.model.Site;
import ua.adeptius.asterisk.utils.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_ERROR_CONNECTING;
import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;
import static ua.adeptius.asterisk.utils.logging.MyLogger.printException;


public class MySqlDao {

    private ComboPooledDataSource cpds;
    public static final String SITE_TABLE = "sites";

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
        String sql = "SELECT * from "+SITE_TABLE;
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
                        phones,
                        set.getString("standart_number"),
                        set.getString("tracking_id"),
                        set.getString("email"),
                        ips,
                        set.getString("password"),
                        set.getInt("time_to_block")
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
        String sql = SqlStatementHelper.createSqlQueryForDeleteSite(name);
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
        String sql = SqlStatementHelper.createSqlQueryForSaveSite(site);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, site.getName() + " сохранён");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка сохранения данных в БД: " + e.getMessage());
            throw new Exception("Ошибка сохранения данных в БД: " + e.getMessage());
        }
    }

    public boolean editSite(Site site) throws Exception {
        Connection connection = getConnection();

        String sqlDelete = SqlStatementHelper.createSqlQueryForDeleteSite(site.getName());
        String sqlSave = SqlStatementHelper.createSqlQueryForSaveSite(site);

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
            log(DB_OPERATIONS, "Ошибка изменения сайта " + site.getName());
            throw new Exception("Ошибка изменения сайта " + site.getName() + ": " + e.getMessage());
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
                log(DB_OPERATIONS, "ОШИБКА УДАЛЕНИЯ НЕНУЖНОЙ ТАБЛИЦЫ С БД " + s);
            }
        }
    }


    public void createStatisticTables(List<String> tablesToCreate) {
        for (String s : tablesToCreate) {
            s = "statistic_" + s;
            String sql = SqlStatementHelper.createSqlQueryForCtreatingStatisticTable(s);
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


    public void saveStatisticToTable(Site site, Statistic statistic) {
        String sql = "INSERT INTO `statistic_"+site.getName()+"` VALUES ('"
                +statistic.getDateForDb()+"', '"
                +statistic.getDirection()+"', '"
                +statistic.getTo()+"', '"
                +statistic.getFrom()+"', '"
                +statistic.getTimeToAnswerInSeconds()+"', '"
                +statistic.getSpeakTimeInSeconds()+"', '"
                +statistic.getGoogleId()+"', '"
                +statistic.getCallUniqueId()+"', '"
                +statistic.getRequest()+"');";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, site.getName() + ": Ошибка при сохранении отчета в БД ");
        }
    }

    public void createOrCleanStatisticsTables() throws Exception{
        List<String> tables = getListOfTables();
        List<String> tablesToDelete = Utils.findTablesThatNeedToDelete(TrackingController.sites, tables);
        deleteTables(tablesToDelete);
        List<String> tablesToCreate = Utils.findTablesThatNeedToCreate(TrackingController.sites, tables);
        createStatisticTables(tablesToCreate);
    }

    public void addIpToBlackList(String siteName, String ip) throws Exception {
        Site site = TrackingController.getSiteByName(siteName);
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
            Site site = TrackingController.getSiteByName(SiteName);
            site.getBlackIps().remove(ip);
            return "IP " + ip + " удалён";
        }else if (s.contains(ip)){
            s = s.replaceAll(ip, "");
            setBlackList(SiteName, s);
            Site site = TrackingController.getSiteByName(SiteName);
            site.getBlackIps().remove(ip);
            return "IP " + ip + " удалён";
        }
        return "IP " + ip + " не заблокирован";
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
            TrackingController.getSiteByName(name).setTimeToBlock(time);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, name + ": Ошибка при установке времени блокировки в БД");
            throw new Exception(name + ": Ошибка при установке времени блокировки в БД");
        }
    }
}
