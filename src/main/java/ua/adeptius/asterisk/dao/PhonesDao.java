package ua.adeptius.asterisk.dao;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;

public class PhonesDao {

    public static ArrayList<String> getCustomersNumbers(String name, boolean innerTable) throws Exception {
        String table = innerTable ? "inner" : "outer";
        String query = "SELECT `number` FROM `" + table + "` where `busy` like '" + name + "'";
        try (Connection connection = TelephonyDao.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(query);
            ArrayList<String> list = new ArrayList<>();
            while (set.next()) {
                list.add(set.getString("number"));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при загрузке внешних номеров для " + name + " c БД");
            throw new Exception("Ошибка при загрузке внешних номеров для " + name + " с БД");
        }
    }

    public static HashMap<String, String> getAllOuterPhones() throws Exception {
        return getPhones("SELECT * FROM `outer`");
    }

    public static HashMap<String, String> getAllInnerPhones() throws Exception {
        return getPhones("SELECT * FROM `inner`");
    }

    public static HashMap<String, String> getBusyOuterPhones() throws Exception {
        return getPhones("SELECT * FROM `outer` where `busy` not like ''");
    }

    public static HashMap<String, String> getBusyInnerPhones() throws Exception {
        return getPhones("SELECT * FROM `inner` where `busy` not like ''");
    }


    private static HashMap<String, String> getPhones(String query) throws Exception {
        try (Connection connection = TelephonyDao.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(query);
            HashMap<String, String> map = new HashMap<>();
            while (set.next()) {
                map.put(set.getString("number"), set.getString("busy"));
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при загрузке внешних номеров с БД");
            throw new Exception("Ошибка при загрузке внешних номеров с БД");
        }
    }

    public static ArrayList<String> getFreePhones(boolean innerTable) throws Exception {
        String table = innerTable ? "inner" : "outer";
        String query = "SELECT * FROM telephonydb.`" + table + "` where `busy` like ''";
        try (Connection connection = TelephonyDao.getConnection();
             Statement statement = connection.createStatement()) {
            ArrayList<String> list = new ArrayList<>();
            ResultSet set = statement.executeQuery(query);
            while (set.next()) {
                list.add(set.getString("number"));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при загрузке свободных номеров с БД");
            throw new Exception("Ошибка при загрузке свободных номеров с БД");
        }
    }

    public static void markNumbersBusy(List<String> numbers, String name, boolean innerTable) throws Exception {
        for (String number : numbers) {
            markNumberBusy(name, number, innerTable);
        }
    }

    private static void markNumberBusy(String name, String number, boolean innerTable) throws Exception {
        String table = innerTable ? "inner" : "outer";
        String sql = "UPDATE `" + table + "` SET `busy`='" + name + "' WHERE `number`='" + number + "';";
        try (Connection connection = TelephonyDao.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
            log(DB_OPERATIONS, "Ошибка при пометке занятости номера в БД");
            throw new Exception("Ошибка при пометке занятости номера в БД");
        }
    }

    public static void markNumberFree(List<String> numbersToRelease, boolean innerTable) throws Exception {
        String table = innerTable ? "inner" : "outer";
        for (String s : numbersToRelease) {
            String sql = "UPDATE `" + table + "` SET `busy`='' WHERE `number`='" + s + "'";
            try (Connection connection = TelephonyDao.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            } catch (Exception e) {
                e.printStackTrace();
                log(DB_OPERATIONS, "Ошибка при освобождении номера в БД");
                throw new Exception("Ошибка при освобождении номера в БД");
            }
        }
    }
}
