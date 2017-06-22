package ua.adeptius.asterisk.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.telephony.SipConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static ua.adeptius.asterisk.utils.logging.LogCategory.DB_OPERATIONS;
import static ua.adeptius.asterisk.utils.logging.MyLogger.log;
import static ua.adeptius.asterisk.utils.logging.MyLogger.logAndThrow;

public class PhonesDao {

    private static Logger LOGGER =  LoggerFactory.getLogger(PhonesDao.class.getSimpleName());


    public static void saveSipToDB(SipConfig sipConfig) throws Exception {
        LOGGER.debug("Сохраняю SIP номер в БД: {}", sipConfig.getNumber());
        String sql = "INSERT INTO `inner` (`number`, `busy`, `password`) VALUES ('"
                + sipConfig.getNumber() + "', '', '" + sipConfig.getPassword() + "');";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            LOGGER.error("Ошибка сохранения SIP номера в БД "+sipConfig.getNumber(), e);
            logAndThrow(DB_OPERATIONS, "Ошибка при сохранении SIP номера");
            throw new Exception("Ошибка при сохранении SIP номера");
        }
    }

    public static int getMaxSipNumber() throws Exception {
        LOGGER.trace("Поиск в базе максимального номера телефона");
        String sql = "SELECT max(number) FROM `inner`";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                int num = set.getInt("max(number)");
                if (num > 2001000) return num;
            }
            return 2001000;
        } catch (Exception e) {
            LOGGER.error("Ошибка получения максимального номера", e);
            logAndThrow(DB_OPERATIONS, "Ошибка при загрузке максимального номера");
            throw new Exception("Ошибка при загрузке максимального номера");
        }
    }

    public static HashMap<String, String> getSipPasswords(String name) throws Exception {
        LOGGER.trace("{}: запрос паролей к сип номерам", name);
        HashMap<String, String> sipPasswords = new HashMap<>();
        String sql = "SELECT `number`, `password` FROM `inner` where `busy`='" + name + "'";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                sipPasswords.put(set.getString("number"), set.getString("password"));
            }
            return sipPasswords;
        } catch (Exception e) {
            LOGGER.error(name+": ошибка получения паролей к сип номерам", e);
            logAndThrow(DB_OPERATIONS, "Ошибка при загрузке паролей телефонии с БД");
            throw new Exception("Ошибка при загрузке паролей телефонии с БД");
        }
    }

  public static HashMap<String, String> getAllSipsAndPass() throws Exception {
        LOGGER.trace("Запрос всех сип из базы.");
        HashMap<String, String> sipPasswords = new HashMap<>();
        String sql = "SELECT `number`, `password` FROM `inner`";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(sql);
            while (set.next()) {
                sipPasswords.put(set.getString("number"), set.getString("password"));
            }
            return sipPasswords;
        } catch (Exception e) {
            LOGGER.error("Ошибка получения всех сип из базы.", e);
            logAndThrow(DB_OPERATIONS, "Ошибка при загрузке номеров SIP с БД");
            throw new Exception("Ошибка при загрузке номеров SIP с БД");
        }
    }

    public static ArrayList<String> getCustomersNumbers(String name, boolean innerTable) throws Exception {
        LOGGER.trace("{}: запрос всех номеров пользователя из {} таблицы", name, innerTable? "внутренней":"внешней");
        String table = innerTable ? "inner" : "outer";
        String query = "SELECT `number` FROM `" + table + "` where `busy` like '" + name + "'";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(query);
            ArrayList<String> list = new ArrayList<>();
            while (set.next()) {
                list.add(set.getString("number"));
            }
            return list;
        } catch (Exception e) {
            LOGGER.error(name,": ошибка получения всех номеров пользователя", e);
            log(DB_OPERATIONS, "Ошибка при загрузке внешних номеров для " + name + " c БД");
            throw new Exception("Ошибка при загрузке внешних номеров для " + name + " с БД");
        }
    }

    public static HashMap<String, String> getBusyOuterPhones() throws Exception {
        LOGGER.trace("Запрос занятых внешних номеров из БД");
        return getPhones("SELECT * FROM `outer` where `busy` not like ''");
    }

    public static HashMap<String, String> getBusyInnerPhones() throws Exception {
        LOGGER.trace("Запрос занятых внутренних номеров из БД");
        return getPhones("SELECT * FROM `inner` where `busy` not like ''");
    }


    private static HashMap<String, String> getPhones(String query) throws Exception {
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery(query);
            HashMap<String, String> map = new HashMap<>();
            while (set.next()) {
                map.put(set.getString("number"), set.getString("busy"));
            }
            return map;
        } catch (Exception e) {
            LOGGER.error("Ошибка загрузки номеров с БД", e);
            log(DB_OPERATIONS, "Ошибка при загрузке внешних номеров с БД");
            throw new Exception("Ошибка при загрузке внешних номеров с БД");
        }
    }

    public static ArrayList<String> getFreePhones(boolean innerTable) throws Exception {
        LOGGER.trace("Запрос свободных номеров  из {} таблицы", innerTable? "внутренней":"внешней");
        String table = innerTable ? "inner" : "outer";
        String query = "SELECT * FROM `" + table + "` where `busy` like ''";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            ArrayList<String> list = new ArrayList<>();
            ResultSet set = statement.executeQuery(query);
            while (set.next()) {
                list.add(set.getString("number"));
            }
            return list;
        } catch (Exception e) {
            LOGGER.error("Ошибка получения свободных номеров с БД", e);
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
        LOGGER.trace("{}: помечаем номер {} занятым", name, number);
        String table = innerTable ? "inner" : "outer";
        String sql = "UPDATE `" + table + "` SET `busy`='" + name + "' WHERE `number`='" + number + "';";
        try (Connection connection = MySqlDao.getTrackingConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            LOGGER.error(name+": ошибка при пометке занятости номера "+number, e);
            log(DB_OPERATIONS, "Ошибка при пометке занятости номера в БД");
            throw new Exception("Ошибка при пометке занятости номера в БД");
        }
    }

    public static void markNumberFree(List<String> numbersToRelease, boolean innerTable) throws Exception {
        String table = innerTable ? "inner" : "outer";
        for (String s : numbersToRelease) {
            LOGGER.trace("Освобождаем номер {}", s);
            String sql = "UPDATE `" + table + "` SET `busy`='' WHERE `number`='" + s + "'";
            try (Connection connection = MySqlDao.getTrackingConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            } catch (Exception e) {
                LOGGER.error("Ошибка при освобождении номера в БД", e);
                log(DB_OPERATIONS, "Ошибка при освобождении номера в БД");
                throw new Exception("Ошибка при освобождении номера в БД");
            }
        }
    }


    public static void deleteNumbersFromDb(List<String> numbersToRelease, boolean innerTable)throws Exception {
        String table = innerTable ? "inner" : "outer";
        for (String s : numbersToRelease) {
            LOGGER.trace("Удаляем из БД номер {}", s);

            String sql = "DELETE FROM `" + table + "` WHERE `number`='" + s + "'";
            try (Connection connection = MySqlDao.getTrackingConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            } catch (Exception e) {
                LOGGER.error("Ошибка при освобождении номера в БД", e);
                log(DB_OPERATIONS, "Ошибка при удалении номера в БД");
                throw new Exception("Ошибка при удалении номера в БД");
            }
        }
    }
}
