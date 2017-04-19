package ua.adeptius.asterisk.dao;


import ua.adeptius.asterisk.controllers.MainController;
import ua.adeptius.asterisk.model.OldSite;
import ua.adeptius.asterisk.model.TelephonyCustomer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;
import static ua.adeptius.asterisk.utils.logging.MyLogger.logAndThrow;

public class MySqlCalltrackDao extends MySqlDao {

    public static List<OldSite> getSites() throws Exception {
        List<OldSite> oldSites = new ArrayList<>();
        String sql = "SELECT * FROM users_calltracking";
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                oldSites.add(new OldSite(
                        set.getString("name"),
                        set.getString("standart_number"),
                        set.getString("tracking_id"),
                        set.getString("email"),
                        DaoHelper.getListFromString(set.getString("black_list_ip")),
                        set.getString("password"),
                        set.getInt("time_to_block"),
                        set.getInt("outer_phones")
                ));
            }
            return oldSites;
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при загрузке сайтов с БД");
        throw new Exception("Ошибка при загрузке сайтов с БД");
    }

    public static void deleteSite(String name) throws Exception {
        String sql = DaoHelper.createSqlQueryForDeleteSite(name);
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log(DB_OPERATIONS, "Ошибка при удалении сайтов с БД");
        throw new Exception("Ошибка при удалении сайтов с БД");
    }

    public static void saveSite(OldSite oldSite) throws Exception {
        String sql = DaoHelper.createSqlQueryForSaveSite(oldSite);
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, oldSite.getName() + " сохранён");
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка сохранения сайта в БД: " + e.getMessage());
            throw new Exception("Ошибка сохранения сайта в БД: " + e.getMessage());
        }
    }

    //TODO сделать атомарным
    public static void editSite(OldSite oldSite) throws Exception {
        Connection connection = getTrackingConnection();
        String sqlDelete = DaoHelper.createSqlQueryForDeleteSite(oldSite.getName());
        String sqlSave = DaoHelper.createSqlQueryForSaveSite(oldSite);
        try (Statement deleteStatement = connection.createStatement();
             Statement addStatement = connection.createStatement()) {
            connection.setAutoCommit(false);
            deleteStatement.execute(sqlDelete);
            addStatement.execute(sqlSave);
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                connection.rollback();
            }
            log(DB_OPERATIONS, "Ошибка изменения сайта в базе" + oldSite.getName());
            throw new Exception("Ошибка изменения сайта " + oldSite.getName() + " в базу: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    // TODO как-то переделать работу с черным списком
    public static void addIpToBlackList(String siteName, String ip) throws Exception {
        OldSite oldSite = MainController.getSiteByName(siteName);
        oldSite.getBlackIps().add(ip);
        String s = getBlackList(siteName);
        s += "," + ip;
        if (s.length() > 1500){
            s = s.substring(s.indexOf(","));
            s = s.substring(s.indexOf(","));
        }
        setBlackList(siteName, s);
    }


    public static String deleteFromBlackList(String SiteName, String ip) throws Exception {
        String s = getBlackList(SiteName);
        if (s.contains(","+ip)){
            s = s.replaceAll(","+ip, "");
            setBlackList(SiteName, s);
            OldSite oldSite = MainController.getSiteByName(SiteName);
            oldSite.getBlackIps().remove(ip);
            return "Success: IP " + ip + " unblocked.";
        }else if (s.contains(ip)){
            s = s.replaceAll(ip, "");
            setBlackList(SiteName, s);
            OldSite oldSite = MainController.getSiteByName(SiteName);
            oldSite.getBlackIps().remove(ip);
            return "Success: IP " + ip + " unblocked.";
        }
        return "Error: IP " + ip + " not blocked.";
    }

    private static void setBlackList(String sitename, String ip) throws Exception {
        String sql = "UPDATE `users_calltracking` SET `black_list_ip`='"+ip+"' WHERE `name`='"+sitename+"';";
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, sitename + ": Ошибка при сохранении черного списка в БД ");
            throw new Exception(sitename + ": Ошибка при сохранении черного списка в БД ");
        }
    }

    private static String getBlackList(String sitename) throws Exception{
        String sql = "SELECT `black_list_ip` FROM `users_calltracking` WHERE `name` like \""+sitename+"\"";
        try (Connection connection = getTrackingConnection();
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


    public static void setTimeToBlock(String name, int time) throws Exception{
        String sql = "UPDATE `users_calltracking` SET `time_to_block`='"+time+"' WHERE `name`='"+name+"';";
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            MainController.getSiteByName(name).setTimeToBlock(time);
        } catch (Exception  e) {
            e.printStackTrace();
            log(DB_OPERATIONS, name + ": Ошибка при установке времени блокировки в БД");
            throw new Exception(name + ": Ошибка при установке времени блокировки в БД");
        }
    }


    public static List<String> getMelodies() throws Exception {
        List<String> melodies = new ArrayList<>();
        String sql = "SELECT * FROM `melodies`";
        try (Connection connection = getTrackingConnection();
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

    public static List<TelephonyCustomer> getTelephonyCustomers() throws Exception {
        List<TelephonyCustomer> telephonyCustomers = new ArrayList<>();
        String sql = "SELECT * FROM users_telephony";
        try (Connection connection = getTrackingConnection();
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


    public static void saveTelephonyCustomer(TelephonyCustomer newCustomer) throws Exception {
        String sql = DaoHelper.getQueryForSaveTelephonyCustomer(newCustomer);
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, newCustomer.getName() + " сохранён");
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка сохранения пользователя в БД: " + e);
            throw new Exception("Ошибка сохранения пользователя в БД: " + e);
        }
    }

    public static void editTelephonyCustomer(TelephonyCustomer newCustomer) throws Exception {
        String sql = DaoHelper.getQueryForEditTelephonyCustomer(newCustomer);
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log(DB_OPERATIONS, newCustomer.getName() + " сохранён");
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка изменения пользователя в БД: " + e);
            throw new Exception("Ошибка изменения пользователя в БД: " + e);
        }
    }

    public static void deleteTelephonyCustomer(String name) throws Exception {
        String sql = DaoHelper.createSqlQueryForDeleteTelephonyCustomer(name);
        try (Connection connection = getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при удалении пользователя с БД");
            throw new Exception("Ошибка при удалении пользователя с БД");
        }
    }
}
